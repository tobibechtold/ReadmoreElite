package de.readmoreelite.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.readmoreelite.R;
import de.readmoreelite.data.ReadmoreClient;
import de.readmoreelite.data.ThreadAdapter;
import de.readmoreelite.model.RMStatus;
import de.readmoreelite.model.RMThread;

public class ThreadOverviewActivity extends AppCompatActivity {

	private ThreadAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thread_overview);

		final ListView listView = (ListView) this.findViewById(R.id.threadList);

		List<RMThread> threads = new ArrayList<RMThread>();
		adapter = new ThreadAdapter(this, android.R.layout.simple_list_item_1,
				threads);
		listView.setAdapter(adapter);
		Bundle extras = getIntent().getExtras();
		final int forenId = extras.getInt("ID_FORUM");
		final int categoryId = extras.getInt("ID_CATEGORY");
		String forumName = extras.getString("NAME_FORUM");
		setTitle(forumName);
		
		final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.thread_overview_swipe_refresh_layout);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				new Handler().post(new Runnable() {
					
					@Override
					public void run() {
						new ThreadOverviewController(ThreadOverviewActivity.this).execute(categoryId, forenId);
						refreshLayout.setRefreshing(false);
					}
				});
			}
		});
		
		

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				RMThread item = adapter.getItem(position);
				Intent i = new Intent(ThreadOverviewActivity.this,
						ThreadActivity.class);
				i.putExtra("ID_FORUM", forenId);
				i.putExtra("ID_CATEGORY", categoryId);
				i.putExtra("ID_THREAD", item.getId());
				i.putExtra("ANZAHL_SEITEN", item.getAnzahlSeiten());
				i.putExtra("SEITE", item.getAnzahlSeiten());
				i.putExtra("THREAD_NAME", item.getTitel());
				startActivity(i);

			}
		});
		new ThreadOverviewController(this).execute(categoryId, forenId);
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

	private class ThreadOverviewController extends
			AsyncTask<Integer, Long, List<RMThread>> {

		private static final String USER_AGENT = "";
		private Activity view;
		private ProgressDialog dialog;
		private HttpClient client;

		public ThreadOverviewController(Activity view) {

			this.view = view;
			dialog = new ProgressDialog(ThreadOverviewActivity.this);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.setMessage("Threads werden geladen...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected List<RMThread> doInBackground(Integer... params) {
			int categoryId = params[0];
			int forumId = params[1];
			String url = "http://46.101.175.47:8182/threads?categoryId="
					+ categoryId + "&forenId=" + forumId;
			List<RMThread> list2 = new ArrayList<RMThread>();
			URL obj;
			try {
				obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj
						.openConnection();

				// optional default is GET
				con.setRequestMethod("GET");

				// add request header
				con.setRequestProperty("User-Agent", USER_AGENT);

				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream(), "UTF-8"));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				String json = response.toString();
				System.out.println(json);

				JSONArray arr = null;
				try {
					arr = new JSONArray(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				client  = ReadmoreClient.getClient();
				
				if(isCancelled()) {
					return null;
				}
				
				HttpGet get = new HttpGet("http://www.readmore.de/forums/" + categoryId + "/" + forumId);
				HttpResponse getResponse = client.execute(get);
				List<String> read = new ArrayList<String>();
				String html = EntityUtils.toString(getResponse.getEntity());

				Document doc = Jsoup.parse(html);
				Elements allThreads = doc.getElementsByClass("forum_threads");
				for(Element thread : allThreads) {
					Elements threads = thread.getElementsByTag("tr");
					for(Element t : threads) {
						
						if(t.getElementsByTag("td").size() > 0) {
							Element td = t.children().get(0);
							String attr = td.getElementsByTag("div").attr("class");
							read.add(attr);
						}
					}
				}

				for (int i = 1; i < arr.length(); i++) {
					RMThread t = convertThread(arr.getJSONObject(i));
					if(read.get(i-1).equals("status sticky_unread "))
						t.setRead(RMStatus.STICKY_UNREAD);
					else if(read.get(i-1).equals("status unread "))
						t.setRead(RMStatus.UNREAD);
					else if(read.get(i-1).equals("status sticky_read "))
						t.setRead(RMStatus.STICKY_READ);
					else if(read.get(i-1).equals("status read "))
						t.setRead(RMStatus.READ);
					else if(read.get(i-1).equals("status unread closed"))
						t.setRead(RMStatus.CLOSED_UNREAD);
                    else if(read.get(i-1).equals("status read closed"))
                        t.setRead(RMStatus.CLOSED);
					
					list2.add(t);
				}

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return list2;

			// print result
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

		private RMThread convertThread(JSONObject jsonObject) {

			RMThread t = new RMThread();
			try {
				t.setTitel(jsonObject.getString("titel"));
				t.setId(jsonObject.getInt("id"));
				t.setAnzahlSeiten(jsonObject.getInt("anzahlSeiten"));
                t.setLetzterBeitrag(jsonObject.getString("letzterBeitrag"));
                t.setLetzterBeitragDatum(jsonObject.getString("letzterBeitragDatum"));
                t.setAnzahlBeitraege(jsonObject.getInt("anzahlBeitraege"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return t;
		}

		@Override
		protected void onPostExecute(List<RMThread> threads) {

			super.onPostExecute(threads);
			if (dialog != null) {
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
			adapter.setItemList(threads);
			adapter.notifyDataSetChanged();
		}

	}
}
