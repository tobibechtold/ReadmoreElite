package de.readmoreelite.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.EntityUtilsHC4;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.readmoreelite.R;
import de.readmoreelite.data.ReadmoreClient;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
	
	public boolean loggedIn = false;
	public static final String PREFS_NAME = "ReadmorePreferences";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String username = preferences.getString(PREF_USERNAME, "");
		String password = preferences.getString(PREF_PASSWORD, "");
		
		if(!username.equals("") && !password.equals("")) {
			EditText txtusername = (EditText) this.findViewById(R.id.editText1);
			EditText txtpasswort = (EditText) this.findViewById(R.id.editText2);
			Button login = (Button) this.findViewById(R.id.button1);
			
			txtusername.setText(username);
			txtpasswort.setText(password);
			login.callOnClick();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	public void login(View view) {
		
		EditText username = (EditText) this.findViewById(R.id.editText1);
		EditText passwort = (EditText) this.findViewById(R.id.editText2);
		CheckBox checkSave = (CheckBox) this.findViewById(R.id.checkBox1);
		
		String user = username.getText().toString();
		String pass = passwort.getText().toString();
		
		if(checkSave.isChecked()) {
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
				.edit()
				.putString(PREF_USERNAME, user)
				.putString(PREF_PASSWORD, pass)
				.commit();
		}
		
		new LoginController().execute(user, pass);
		
		
	}
	
	private class LoginController extends AsyncTask<String, Void, Boolean> {
		
		private HttpClient client = ReadmoreClient.getClient();
		private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setMessage("Einloggen...");
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params){
			String user = params[0];
			String pass = params[1];
			String url = "http://www.readmore.de/users/login";
			String page = "";
			try {
				page = GetPageContent(url);
				List<NameValuePair> loginParams = getFormParams(page,
						user, pass);
				sendPost(url, loginParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (dialog != null) {
				if (dialog.isShowing()) {
					dialog.dismiss();
					dialog = null;
				}
			}
			if(loggedIn) {
				Intent intent = new Intent(getApplicationContext(), ForumOverviewActivity.class);
				startActivity(intent);
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setMessage("Benutzername oder Passwort falsch").setTitle("Login fehlgeschlagen").setPositiveButton("Ok", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		}
		
		private void sendPost(String login,
				List<NameValuePair> loginParams) throws Exception {

			HttpPost post = new HttpPost(login);

			post.setEntity(new UrlEncodedFormEntity(loginParams, "UTF-8"));

			HttpResponse response = client.execute(post);
			
			Header[] headers = response.getHeaders("Location");
			
			EntityUtilsHC4.consume(response.getEntity());
			
			if(headers.length > 0) {
				String value = headers[0].getValue();
				if(value.equals("http://www.readmore.de/users")) {
					loggedIn = true;
				}
			}
		}
		
		private String GetPageContent(String url) throws Exception {

			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);
			
			return EntityUtils.toString(response.getEntity());

		}
		
		public List<NameValuePair> getFormParams(String html, String username,
				String password) throws UnsupportedEncodingException {

			System.out.println("Extracting form's data...");

			Document doc = Jsoup.parse(html);

			// Google form id
			Elements loginform = doc.getElementsByTag("form");
			Elements inputElements = loginform.get(0).getElementsByTag("input");
			String crypt = inputElements.get(0)
					.getElementsByAttributeValue("name", "crypt").val();
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			for (Element inputElement : inputElements) {
				String key = inputElement.attr("name");
				String value = inputElement.attr("value");

				if (!key.equals("")) {
					if (key.equals("user_name"))
						value = username;
					else if (key.equals("user_passwd"))
						value = password;
					else if (key.equals("crypt"))
						value = crypt;
					else if (key.equals("post"))
						value = "1";
					paramList.add(new BasicNameValuePair(key, value));
				}
			}

			// build parameters list

			return paramList;
		}
		
	}
}
