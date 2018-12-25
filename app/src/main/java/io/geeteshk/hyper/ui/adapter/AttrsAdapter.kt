package io.geeteshk.hyper.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import io.geeteshk.hyper.R
import io.geeteshk.hyper.extensions.inflate
import kotlinx.android.synthetic.main.dialog_input_single.view.*
import kotlinx.android.synthetic.main.item_attr.view.*
import org.eclipse.jgit.util.StringUtils
import org.jsoup.nodes.Element
import java.util.*
import java.util.regex.Pattern

class AttrsAdapter(private val context: Context, private val currentElement: Element) : RecyclerView.Adapter<AttrsAdapter.AttrsHolder>() {
    private val attrsList: MutableList<String>

    init {
        val patternBuiltins = Pattern.compile("\\b(charset|lang|href|onclick|onmouseover|onmouseout|code|codebase|width|height|align|vspace|hspace|name|archive|mayscript|alt|shape|coords|target|nohref|size|color|face|src|loop|bgcolor|background|text|vlink|alink|bgproperties|topmargin|leftmargin|marginheight|marginwidth|onload|onunload|onfocus|onblur|stylesrc|scroll|clear|type|value|valign|span|compact|pluginspage|pluginurl|hidden|autostart|playcount|volume|controller|mastersound|starttime|endtime|point-size|weight|action|method|enctype|onsubmit|onreset|scrolling|noresize|frameborder|bordercolor|cols|rows|framespacing|border|noshade|longdesc|ismap|usemap|lowsrc|naturalsizeflag|nosave|dynsrc|controls|start|suppress|maxlength|checked|language|onchange|onkeypress|onkeyup|onkeydown|autocomplete|prompt|for|rel|rev|media|direction|behaviour|scrolldelay|scrollamount|http-equiv|content|gutter|defer|event|multiple|readonly|cellpadding|cellspacing|rules|bordercolorlight|bordercolordark|summary|colspan|rowspan|nowrap|halign|disabled|accesskey|tabindex|id|class)")
        attrsList = LinkedList(Arrays.asList(*patternBuiltins.pattern().replace("(", "").replace(")", "").substring(2, patternBuiltins.pattern().length - 2).split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        attrsList.sort()
        sortByPriority(attrsList)
    }

    private fun sortByPriority(attributes: MutableList<String>) {
        for (i in attributes.indices) {
            val attribute = attributes[i]
            if (!StringUtils.isEmptyOrNull(currentElement.attr(attribute))) {
                attributes.remove(attribute)
                attributes.add(0, attribute)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttrsHolder {
        val rootView = parent.inflate(R.layout.item_attr)
        return AttrsHolder(rootView)
    }

    override fun onBindViewHolder(holder: AttrsHolder, position: Int) = if (position == 0) {
        holder.attrKey.setText(R.string.key)
        holder.attrKey.typeface = Typeface.DEFAULT_BOLD
        holder.attrValue.setText(R.string.value)
        holder.attrValue.typeface = Typeface.DEFAULT_BOLD
        holder.attrEdit.visibility = View.GONE
    } else {
        val newPos = holder.adapterPosition - 1
        holder.attrKey.text = attrsList[newPos]
        holder.attrValue.text = currentElement.attr(attrsList[newPos])
        holder.attrEdit.setOnClickListener {
            val attrsView = View.inflate(context, R.layout.dialog_input_single, null)
            attrsView.inputText.setHint(R.string.value)
            attrsView.inputText.setSingleLine(true)
            attrsView.inputText.maxLines = 1
            attrsView.inputText.setText(currentElement.attr(attrsList[newPos]))

            AlertDialog.Builder(context)
                    .setTitle(attrsList[newPos])
                    .setView(attrsView)
                    .setPositiveButton("SET") { _, _ ->
                        currentElement.attr(attrsList[newPos], attrsView.inputText.text.toString())
                        holder.attrValue.text = attrsView.inputText.text.toString()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = attrsList.size + 1

    inner class AttrsHolder(view: View) : RecyclerView.ViewHolder(view) {
        val attrKey: TextView = view.attrKey
        val attrValue: TextView = view.attrValue
        val attrEdit: ImageButton = view.attrEdit
    }
}
