package de.readmoreelite.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.EntityUtilsHC4;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import de.readmoreelite.R;
import de.readmoreelite.data.BeitragAdapter;
import de.readmoreelite.data.ReadmoreClient;
import de.readmoreelite.model.Beitrag;
import de.readmoreelite.model.User;

public class ThreadActivity extends AppCompatActivity {

	private BeitragAdapter adapter;
	private int seite;
	private int threadId;
	private int categoryId;
	private int forenId;
	private Spinner seitenSpinner;
	private EditText textPost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_thread);
		
		final ListView listView = (ListView) this.findViewById(R.id.listView1);
		
		List<Beitrag> threads = new ArrayList<Beitrag>();
		ImageButton btnSend = (ImageButton) this.findViewById(R.id.imageButton);
		textPost = (EditText) this.findViewById(R.id.editPost);
		textPost.setMaxLines(4);
		textPost.setHorizontallyScrolling(false);
		adapter = new BeitragAdapter(this, 
				android.R.layout.simple_list_item_1, 
				threads, textPost);
		listView.setAdapter(adapter);
		listView.setItemsCanFocus(true);
		listView.setScrollingCacheEnabled(false);
		forenId = 10;
		categoryId = 91;
		threadId = 0;
		int anzahlSeiten = 1;
		seite = 1;
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			forenId = extras.getInt("ID_FORUM");
			categoryId = extras.getInt("ID_CATEGORY");
			threadId = extras.getInt("ID_THREAD");
			anzahlSeiten = extras.getInt("ANZAHL_SEITEN");
			seite = extras.getInt("SEITE");
			String name = extras.getString("THREAD_NAME");
			setTitle(name);
		}
		
		seitenSpinner = (Spinner) this.findViewById(R.id.page_spinner);
		Integer[] seiten = new Integer[anzahlSeiten];
		for(int i = 0; i < anzahlSeiten; i++) {
			seiten[i] = i+1;
		}
		final ArrayAdapter<Integer> seitenAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, seiten);
		seitenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		seitenSpinner.setAdapter(seitenAdapter);
		new ThreadController().execute(categoryId, forenId, threadId, seite);
		seitenSpinner.setSelection(getSeite());
		seitenSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				int s = getSeite();
				if (s != position) {
					int seite = seitenAdapter.getItem(position);
					setSeite(position);
					Intent intent = getIntent();
					intent.removeExtra("SEITE");
					intent.putExtra("SEITE", seite);
					finish();
					startActivity(intent);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});
		
		btnSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage(textPost.getText().toString());
			}
		});
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}
	
	private void sendMessage(String message) {
		if(!message.equals("")) {
			new PostController(this.categoryId, this.forenId, this.threadId).execute(message);
		}
	}

	public int getSeite() {
		return seite - 1;
	}

	public void setSeite(int seite) {
		this.seite = seite;
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
	
	private class ThreadController extends AsyncTask<Integer, Long, List<Beitrag>>{
		
		private static final String USER_AGENT = "";
		private ProgressDialog dialog;
		
		public ThreadController() {
			
			dialog = new ProgressDialog(ThreadActivity.this);
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
		protected List<Beitrag> doInBackground(Integer... params) {
			int categoryId = params[0];
			int forumId = params[1];
			int threadId = params[2];
			int seite = params[3];
			String url = "http://46.101.175.47:8182/beitrag?categoryId=" + categoryId + "&forenId=" + forumId + "&threadId=" + threadId + "&seite=" + seite;
			List<Beitrag> list2 = new ArrayList<Beitrag>();
			URL obj;
			try {
				obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				 
				// optional default is GET
				con.setRequestMethod("GET");
		 
				//add request header
				con.setRequestProperty("User-Agent", USER_AGENT);
		 
				int responseCode = con.getResponseCode();
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);
		 
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream(), "UTF-8"));
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
				
				for (int i = 0; i < arr.length(); i++) {
					list2.add(convertBeitrag(arr.getJSONObject(i)));
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
			
	 
			//print result
		}
		
		private Beitrag convertBeitrag(JSONObject jsonObject) {
			
			Beitrag t = new Beitrag();
			User u = new User();
			try {
				
				String inhalt = jsonObject.getString("inhalt");
				t.setOriginalInhalt(inhalt);
				TextProcessor bb = BBProcessorFactory.getInstance().createFromResource("res/raw/configuration.xml");
				String inhaltKonvertiert = bb.process(inhalt);
				t.setInhalt(inhaltKonvertiert);
				t.setTag(jsonObject.getString("tag"));
				t.setUhrzeit(jsonObject.getString("uhrzeit"));
				t.setBeitragNummer(jsonObject.getString("beitragNummer"));
				
				t.setThreadId(jsonObject.getInt("threadId"));
				u.setAnzeigename(jsonObject.getJSONObject("ersteller").getString("anzeigename"));
				u.setAvatar(jsonObject.getJSONObject("ersteller").getString("avatar"));
				t.setErsteller(u);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return t;
		}


		@Override
		protected void onPostExecute(List<Beitrag> threads) {
			
			super.onPostExecute(threads);
			if(dialog != null) {
				if(dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
			adapter.setItemList(threads);
			adapter.notifyDataSetChanged();
		}



	}

	private class PostController extends AsyncTask<String, Void, Void> {
		
		private int categoryId;
		private int forumId;
		private int threadId;
		private HttpClient client = ReadmoreClient.getClient();
		private ProgressDialog dialog = new ProgressDialog(ThreadActivity.this);

		public PostController(int categoryId, int forumId, int threadId) {
			super();
			this.categoryId = categoryId;
			this.forumId = forumId;
			this.threadId = threadId;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.setMessage("Sende Antwort...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			String url = "http://www.readmore.de/forums/" + categoryId + "/" + forumId + "/" + threadId;
			try {
				String html = GetPageContent(url);
				List<NameValuePair> postFormParams = getPostFormParams(html, params[0]);
				Document document = Jsoup.connect(url).get();
				String url1 = document.baseUri();
				sendPost(url1, postFormParams);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (dialog != null) {
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent i = getIntent();
			finish();
			startActivity(i);
		}
		
		private void sendPost(String url, List<NameValuePair> postFormParams) throws ClientProtocolException, IOException {
			HttpPost post = new HttpPost(url);
			post.setEntity(new UrlEncodedFormEntity(postFormParams, "UTF-8"));
			HttpResponse postResponse = client.execute(post);
			HttpEntity entity = postResponse.getEntity();
			EntityUtilsHC4.consume(entity);
		}



		private String GetPageContent(String url) throws Exception {

			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			
			return EntityUtils.toString(response.getEntity());

		}
		
		public List<NameValuePair> getPostFormParams(String html, String text)
				throws UnsupportedEncodingException {

			Document doc = Jsoup.parse(html);

			Elements postForm = doc.getElementsByTag("form");
			if(postForm.size() > 0) {
				Elements inputElements = postForm.get(0).getElementsByTag("input");
				String crypt = inputElements.get(0)
						.getElementsByAttributeValue("name", "crypt").val();
				Element textArea = postForm.get(0).getElementById("post_text_0");
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (Element inputElement : inputElements) {
					String key = inputElement.attr("name");
					String value = inputElement.attr("value");
	
					if (!key.equals("")) {
						if (key.equals("post_text_0"))
							value = text;
						else if (key.equals("crypt"))
							value = crypt;
						else if (key.equals("post"))
							value = "1";
						paramList.add(new BasicNameValuePair(key, value));
					}
				}
				
				paramList.add(new BasicNameValuePair("post_text_0", text));
	
				// build parameters list
	
				return paramList;
			}
			return null;
		}
		
	}
}
