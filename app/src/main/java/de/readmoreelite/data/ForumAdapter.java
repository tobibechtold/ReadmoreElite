package de.readmoreelite.data;

import java.util.List;

import de.readmoreelite.model.Forum;
import de.readmoreelite.model.RMStatus;
import de.readmoreelite.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ForumAdapter extends ArrayAdapter<Forum> {
	
	private List<Forum> itemList;
	private Context context;

	public ForumAdapter(Context context,
			int textViewResourceId, List<Forum> objects) {
		
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
	public Forum getItem(int position) {
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
			v = inflater.inflate(R.layout.listitem, null);
		}
		
		Forum c = itemList.get(position);
		TextView text = (TextView) v.findViewById(R.id.txtViewTitle);
		TextView description = (TextView) v.findViewById(R.id.txtViewDescription);
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
		text.setText(c.getTitel());
		description.setText(c.getBeschreibung());
		return v;
	}

	public List<Forum> getItemList() {
		return itemList;
	}

	public void setItemList(List<Forum> itemList) {
		this.itemList = itemList;
	}

}
