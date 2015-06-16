package de.readmoreelite.data;

import java.util.List;

import de.readmoreelite.R;
import de.readmoreelite.model.RMStatus;
import de.readmoreelite.model.RMThread;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ThreadAdapter extends ArrayAdapter<RMThread> {
	
	private List<RMThread> itemList;
	private Context context;

	public ThreadAdapter(Context context,
			int textViewResourceId, List<RMThread> objects) {
		
		super(context, textViewResourceId, objects);
		this.itemList = objects;
		this.context = context;
	}
	
	public int getCount() {
		return itemList.size();
	}
	
	public long getItemId(int position) {
		
		return itemList.get(position).getId();
	}
	
	@Override
	public RMThread getItem(int position) {
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
			v = inflater.inflate(R.layout.listitem_thread, null);
		}
		
		RMThread c = itemList.get(position);
		TextView text = (TextView) v.findViewById(R.id.txtViewTitle);
        TextView textDescription = (TextView) v.findViewById(R.id.txtViewDescription);
        TextView textCount = (TextView) v.findViewById(R.id.txtViewCount);
		ImageView image = (ImageView) v.findViewById(R.id.imgViewLogo);
		
		if(c.getRead() == RMStatus.READ) {
			image.setImageResource(R.drawable.ic_read);
		}
		else if(c.getRead() == RMStatus.UNREAD) {
			image.setImageResource(R.drawable.ic_unread);
		}
		else if(c.getRead() == RMStatus.STICKY_READ) {
			image.setImageResource(R.drawable.ic_sticky_read);
		}
		else if(c.getRead() == RMStatus.STICKY_UNREAD) {
			image.setImageResource(R.drawable.ic_sticky_unread);
		}
		else {
			image.setImageResource(R.drawable.ic_closed_unread);
		}
		text.setText(c.getTitel());
        textDescription.setText(c.getLetzterBeitragDatum() + " - " + c.getLetzterBeitrag());
        textCount.setText("" + c.getAnzahlBeitraege());
		return v;
	}

	public List<RMThread> getItemList() {
		return itemList;
	}

	public void setItemList(List<RMThread> itemList) {
		this.itemList = itemList;
	}

	
}
