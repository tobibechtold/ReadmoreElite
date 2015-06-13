package de.readmoreelite.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.readmoreelite.R;
import de.readmoreelite.data.ForumAdapter;
import de.readmoreelite.data.ReadmoreClient;
import de.readmoreelite.model.Forum;
import de.readmoreelite.model.RMStatus;

public class ForumOverviewActivity extends AppCompatActivity {

	private ForumAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forum_overview);

		final ListView listView = (ListView) this.findViewById(R.id.forumList);

		setTitle("Forum");

		List<Forum> threads = new ArrayList<Forum>();
		adapter = new ForumAdapter(this, android.R.layout.simple_list_item_1,
				threads);
		listView.setAdapter(adapter);

		final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) this
				.findViewById(R.id.forum_overview_swipe_refresh_layout);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().post(new Runnable() {

					@Override
					public void run() {
						new ForumOverviewController(ForumOverviewActivity.this)
								.execute();
						refreshLayout.setRefreshing(false);
					}
				});
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Forum item = adapter.getItem(position);
				Intent i = new Intent(ForumOverviewActivity.this,
						ThreadOverviewActivity.class);
				i.putExtra("ID_FORUM", item.getId());
				i.putExtra("ID_CATEGORY", item.getIdKategorie());
				i.putExtra("NAME_FORUM", item.getTitel());
				startActivity(i);

			}
		});

		new ForumOverviewController(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.forum_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ForumOverviewController extends
			AsyncTask<Void, Long, List<Forum>> {

		private static final String HTTP_WWW_READMORE_DE_FORUMS = "http://www.readmore.de/forums"; 
		private static final String USER_AGENT = "";
		private Activity view;
		private ProgressDialog dialog;

		public ForumOverviewController(Activity view) {

			this.view = view;
			dialog = new ProgressDialog(ForumOverviewActivity.this);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.setMessage("Forum wird geladen...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected List<Forum> doInBackground(Void... params) {
			String url = "http://46.101.175.47:8182/forum";
			List<Forum> list2 = new ArrayList<Forum>();
			JSONArray jsonarray = null;
			try {
				String json = downloadUrl(url);
				Log.v("ForumOverviewActivity", json);

				jsonarray = new JSONArray(json);

				String readmoreHTML = getReadmoreForumsHTML();
				List<String> readForums = findReadForums(Jsoup.parse(readmoreHTML));

				if(isCancelled()) {
					return null;
				}
				
				// TODO von anfang an in entsprechende Map legen
				for (int i = 0; i < jsonarray.length(); i++) {
					Forum f = convertForum(jsonarray.getJSONObject(i));
					String status = readForums.get(i);
					if (status.equals("status unread")) {
						f.setRead(RMStatus.UNREAD);
					} else if (status.equals("status read")){
						f.setRead(RMStatus.READ);
					}
					else if(status.equals("sticky read")) {
						f.setRead(RMStatus.STICKY_READ);
					}
					else if(status.equals("sticky unread")) {
						f.setRead(RMStatus.STICKY_UNREAD);
					}
					list2.add(f);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return list2;
		}

		private List<String> findReadForums(Document dom) {
			List<String> readForums = new ArrayList<String>();
			for (Element element : dom.getElementsByClass("forum_forums")) {
				Elements foren = element.getElementsByTag("tr");
				for (Element forum : foren) {
					if (elementHasData(forum)) {
						Element attribute = forum.children().get(0);
						Element children = attribute.children().get(0);
						readForums.add(children.attr("class"));
					}
				}
			}
			return readForums;
		}

		private boolean elementHasData(Element forum) {
			return forum.getElementsByTag("td").size() > 0;
		}

		private String getReadmoreForumsHTML() throws IOException,
				ClientProtocolException {
			HttpClient client = ReadmoreClient.getClient();
			HttpGet get = new HttpGet(HTTP_WWW_READMORE_DE_FORUMS);
			HttpResponse getResponse = client.execute(get);
			HttpEntity entity = getResponse.getEntity();
			String html = EntityUtils.toString(entity);
			EntityUtilsHC4.consume(entity);
			return html;
		}

		private String downloadUrl(String myUrl) throws IOException {
			InputStream is = null;
			try {
				URL url = new URL(myUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				int response = conn.getResponseCode();
				Log.d("ForumOverviewActivity", "The response is: " + response);
				is = conn.getInputStream();

				// Convert the InputStream into a string
				return convertStreamToString(is);

				// Makes sure that the InputStream is closed after the app is
				// finished using it.
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			if (dialog != null) {
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
		}

		private Forum convertForum(JSONObject jsonObject) {

			Forum f = new Forum();
			try {
				f.setTitel(jsonObject.getString("titel"));
				f.setId(jsonObject.getInt("id"));
				f.setIdKategorie(jsonObject.getInt("idKategorie"));
				f.setBeschreibung(jsonObject.getString("beschreibung"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return f;
		}

		@Override
		protected void onPostExecute(List<Forum> forum) {

			super.onPostExecute(forum);
			if (dialog != null) {
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
			adapter.setItemList(forum);
			adapter.notifyDataSetChanged();
		}

	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}