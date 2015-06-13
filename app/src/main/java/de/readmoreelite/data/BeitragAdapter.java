package de.readmoreelite.data;

import java.util.List;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;
import org.kefirsf.bb.TextProcessorFactory;
import org.kefirsf.bb.proc.BBProcessor;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.readmoreelite.R;
import de.readmoreelite.model.Beitrag;

public class BeitragAdapter extends ArrayAdapter<Beitrag> {
	
	private List<Beitrag> itemList;
	private Context context;
	private EditText editText;

	public BeitragAdapter(Context context,
			int textViewResourceId, List<Beitrag> objects, EditText text) {
		
		super(context, textViewResourceId, objects);
		this.itemList = objects;
		this.context = context;
		this.editText = text;
	}
	
	public int getCount() {
		return itemList.size();
	}
	
	public long getItemId(int position) {
		
		return itemList.get(position).getId();
	}
	
	@Override
	public Beitrag getItem(int position) {
		// TODO Auto-generated method stub
		if(itemList != null) {
			return itemList.get(position);
		}
		return null;
	}
	
	@Override
	public boolean hasStableIds() {
		
		return true;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.listitem_beitrag, parent, false);
		}
		
		final Beitrag c = itemList.get(position);
		Button btnZitieren = (Button) v.findViewById(R.id.btnZitieren);
		TextView text = (TextView) v.findViewById(R.id.textView1);
		btnZitieren.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Beitrag b = c;
				editText.append("[quote=" + b.getErsteller().getAnzeigename() + "]"+b.getOriginalInhalt()+"[/quote]");
			}
		});
		text.setTypeface(null, Typeface.BOLD);
		text.setText(c.getErsteller().getAnzeigename());
		TextView text2 = (TextView) v.findViewById(R.id.textView2);
		text2.setMovementMethod(LinkMovementMethod.getInstance());
		Spanned fromHtml = Html.fromHtml(c.getInhalt());
		text2.setText(fromHtml);
		return v;
	}

	public List<Beitrag> getItemList() {
		return itemList;
	}

	public void setItemList(List<Beitrag> itemList) {
		this.itemList = itemList;
	}


}
