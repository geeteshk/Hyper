package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.eclipse.jgit.util.StringUtils;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;

public class AttrsAdapter extends RecyclerView.Adapter<AttrsAdapter.AttrsHolder> {

    private Element currentElement;
    private List<String> attrsList;
    private Context context;

    public AttrsAdapter(Context context, Element element) {
        this.context = context;
        currentElement = element;
        Pattern patternBuiltins = Pattern.compile("\\b(charset|lang|href|onclick|onmouseover|onmouseout|code|codebase|width|height|align|vspace|hspace|name|archive|mayscript|alt|shape|coords|target|nohref|size|color|face|src|loop|bgcolor|background|text|vlink|alink|bgproperties|topmargin|leftmargin|marginheight|marginwidth|onload|onunload|onfocus|onblur|stylesrc|scroll|clear|type|value|valign|span|compact|pluginspage|pluginurl|hidden|autostart|playcount|volume|controller|mastersound|starttime|endtime|point-size|weight|action|method|enctype|onsubmit|onreset|scrolling|noresize|frameborder|bordercolor|cols|rows|framespacing|border|noshade|longdesc|ismap|usemap|lowsrc|naturalsizeflag|nosave|dynsrc|controls|start|suppress|maxlength|checked|language|onchange|onkeypress|onkeyup|onkeydown|autocomplete|prompt|for|rel|rev|media|direction|behaviour|scrolldelay|scrollamount|http-equiv|content|gutter|defer|event|multiple|readonly|cellpadding|cellspacing|rules|bordercolorlight|bordercolordark|summary|colspan|rowspan|nowrap|halign|disabled|accesskey|tabindex|id|class)");
        attrsList = new LinkedList<>(Arrays.asList(patternBuiltins.pattern().replace("(", "").replace(")", "").substring(2, patternBuiltins.pattern().length() - 2).split("\\|")));
        Collections.sort(attrsList);
        sortByPriority(attrsList);
    }

    private void sortByPriority(List<String> attributes) {
        for (int i = 0; i < attributes.size(); i++) {
            String attribute = attributes.get(i);
            if (!StringUtils.isEmptyOrNull(currentElement.attr(attribute))) {
                attributes.remove(attribute);
                attributes.add(0, attribute);
            }
        }
    }

    @Override
    public AttrsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attr, parent, false);
        return new AttrsHolder(rootView);
    }

    @Override
    public void onBindViewHolder(final AttrsHolder holder, final int position) {
        if (position == 0) {
            holder.attrKey.setText(R.string.key);
            holder.attrKey.setTypeface(Typeface.DEFAULT_BOLD);
            holder.attrValue.setText(R.string.value);
            holder.attrValue.setTypeface(Typeface.DEFAULT_BOLD);
            holder.attrEdit.setVisibility(View.GONE);
        } else {
            final int newPos = holder.getAdapterPosition() - 1;
            holder.attrKey.setText(attrsList.get(newPos));
            holder.attrValue.setText(currentElement.attr(attrsList.get(newPos)));
            holder.attrEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View attrsView = View.inflate(context, R.layout.dialog_input_single, null);
                    final TextInputEditText editText = attrsView.findViewById(R.id.input_text);
                    builder.setTitle(attrsList.get(newPos));
                    editText.setHint(R.string.value);
                    editText.setSingleLine(true);
                    editText.setMaxLines(1);
                    editText.setText(currentElement.attr(attrsList.get(newPos)));
                    builder.setView(attrsView);
                    builder.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            currentElement.attr(attrsList.get(newPos), editText.getText().toString());
                            holder.attrValue.setText(editText.getText().toString());
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, null);
                    builder.create().show();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return attrsList.size() + 1;
    }

    class AttrsHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.attr_key) TextView attrKey;
        @BindView(R.id.attr_value) TextView attrValue;
        @BindView(R.id.element_attr_edit) ImageButton attrEdit;

        AttrsHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
