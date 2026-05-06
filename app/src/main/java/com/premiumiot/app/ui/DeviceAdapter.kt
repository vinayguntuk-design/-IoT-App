package com.premiumiot.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.premiumiot.app.R
import com.premiumiot.app.data.DeviceControl
import com.premiumiot.app.data.DeviceTemplate
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.content
import kotlinx.serialization.json.floatOrNull

class DeviceAdapter(
    private val onControl: (DeviceTemplate, String, JsonElement) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    private var items: List<DeviceCardModel> = emptyList()

    fun submit(next: List<DeviceCardModel>) {
        items = next
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device_card, parent, false)
        return DeviceViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.deviceNameText)
        private val statusText: TextView = view.findViewById(R.id.deviceStatusText)
        private val powerButton: TextView = view.findViewById(R.id.powerButton)
        private val controls: LinearLayout = view.findViewById(R.id.controlContainer)

        fun bind(card: DeviceCardModel) {
            val template = card.template
            val state = card.state
            val power = state.values["power"]?.asBoolean() == true

            nameText.text = template.deviceName
            statusText.text = "${template.room} - ${state.transport.name} - ${if (state.online) "Online" else "Syncing"}"
            powerButton.setBackgroundResource(if (power) R.drawable.bg_power_on else R.drawable.bg_power_off)
            powerButton.setOnClickListener {
                onControl(template, "power", JsonPrimitive(!power))
            }

            controls.removeAllViews()
            template.controls.forEach { control -> renderControl(template, control, state.values[control.key()]) }
        }

        private fun renderControl(template: DeviceTemplate, control: DeviceControl, value: JsonElement?) {
            when (control.type.lowercase()) {
                "toggle" -> controls.addView(toggleRow(template, control, value.asBoolean()))
                "slider" -> controls.addView(sliderRow(template, control, value.asFloat() ?: control.min))
                "button" -> controls.addView(buttonRow(template, control))
                "value" -> controls.addView(valueRow(control, value?.asLabel() ?: "--"))
                "scheduler" -> controls.addView(schedulerRow(template, control))
            }
        }

        private fun toggleRow(template: DeviceTemplate, control: DeviceControl, checked: Boolean): View {
            return SwitchMaterial(itemView.context).apply {
                text = control.label
                isChecked = checked
                setTextColor(itemView.context.getColor(R.color.text_primary))
                setOnCheckedChangeListener { _, isChecked ->
                    onControl(template, control.key(), JsonPrimitive(isChecked))
                }
            }
        }

        private fun sliderRow(template: DeviceTemplate, control: DeviceControl, current: Float): View {
            val row = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 8.dp, 0, 8.dp)
            }
            val label = TextView(itemView.context).apply {
                text = "${control.label}: ${current.toInt()}${control.unit}"
                setTextColor(itemView.context.getColor(R.color.text_primary))
                textSize = 14f
            }
            val slider = Slider(itemView.context).apply {
                valueFrom = control.min
                valueTo = control.max
                stepSize = control.step
                value = current.coerceIn(control.min, control.max)
                addOnChangeListener { _, newValue, fromUser ->
                    label.text = "${control.label}: ${newValue.toInt()}${control.unit}"
                    if (fromUser) onControl(template, control.key(), JsonPrimitive(newValue.toInt()))
                }
            }
            row.addView(label)
            row.addView(slider)
            return row
        }

        private fun buttonRow(template: DeviceTemplate, control: DeviceControl): View {
            return MaterialButton(itemView.context).apply {
                text = control.label
                cornerRadius = 22.dp
                setTextColor(itemView.context.getColor(R.color.black))
                setBackgroundColor(itemView.context.getColor(R.color.accent_yellow))
                setOnClickListener { onControl(template, control.key(), JsonPrimitive(true)) }
            }
        }

        private fun valueRow(control: DeviceControl, value: String): View {
            return TextView(itemView.context).apply {
                text = "${control.label}: $value${control.unit}"
                setTextColor(itemView.context.getColor(R.color.accent_blue))
                textSize = 15f
                setPadding(0, 8.dp, 0, 8.dp)
            }
        }

        private fun schedulerRow(template: DeviceTemplate, control: DeviceControl): View {
            return MaterialButton(itemView.context).apply {
                text = "Schedule"
                cornerRadius = 22.dp
                setTextColor(itemView.context.getColor(R.color.text_primary))
                setStrokeColorResource(R.color.accent_blue)
                strokeWidth = 1.dp
                setOnClickListener {
                    onControl(template, control.key(), JsonPrimitive("20:00:on,23:00:off"))
                }
            }
        }
    }
}

private fun DeviceControl.key(): String = topic.trim('/').ifBlank { label.lowercase() }

private fun JsonElement?.asBoolean(): Boolean = (this as? JsonPrimitive)?.let {
    it.booleanOrNull ?: (it.content == "1" || it.content.equals("on", ignoreCase = true))
} ?: false

private fun JsonElement?.asFloat(): Float? = (this as? JsonPrimitive)?.floatOrNull

private fun JsonElement.asLabel(): String = (this as? JsonPrimitive)?.content ?: toString()
