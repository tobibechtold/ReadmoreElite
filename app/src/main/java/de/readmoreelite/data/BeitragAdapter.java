package de.readmoreelite.data;

import java.io.InputStream;
import java.util.List;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;
import org.kefirsf.bb.TextProcessorFactory;
import org.kefirsf.bb.proc.BBProcessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
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
		ImageView btnZitieren = (ImageView) v.findViewById(R.id.btnZitieren);
		CircleImageView avatarImage = (CircleImageView) v.findViewById(R.id.profile_image);
		new AvatarDownloadTask(avatarImage).execute(c.getErsteller().getAvatar());
		TextView txtUsername = (TextView) v.findViewById(R.id.textView1);
		TextView txtDatum = (TextView) v.findViewById(R.id.textViewDatum);
		TextView txtNummer = (TextView) v.findViewById(R.id.textViewNumber);
		btnZitieren.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Beitrag b = c;
				editText.append("[quote=" + b.getErsteller().getAnzeigename() + "]" + b.getOriginalInhalt() + "[/quote]");
			}
		});
		txtUsername.setTypeface(null, Typeface.BOLD);
		txtUsername.setText(c.getErsteller().getAnzeigename());
		txtDatum.setText(c.getTag() + ", " + c.getUhrzeit());
		txtNummer.setText(c.getBeitragNummer());
		TextView inhalt = (TextView) v.findViewById(R.id.textView2);
		inhalt.setMovementMethod(LinkMovementMethod.getInstance());
		Spanned fromHtml = Html.fromHtml(c.getInhalt());
		inhalt.setText(fromHtml);
		return v;
	}

	public List<Beitrag> getItemList() {
		return itemList;
	}

	public void setItemList(List<Beitrag> itemList) {
		this.itemList = itemList;
	}

	private class AvatarDownloadTask extends AsyncTask<String, Void, Bitmap> {

		private final CircleImageView imageView;

		public AvatarDownloadTask(CircleImageView imageView) {
			this.imageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String urldisplay = params[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			imageView.setImageBitmap(bitmap);
		}
	}


		}
