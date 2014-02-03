package app.of.wonder.assignment3;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static View inputView, storyView;
        private static SharedPreferences prefs;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            inputView = rootView.findViewById(R.id.input_screen);
            ViewGroup input_fields = (ViewGroup) inputView.findViewById(R.id.input_fields);
            storyView = rootView.findViewById(R.id.story_screen);
            gatherData(getResources().getString(R.string.sample_story), input_fields);
            return rootView;
        }

        private String gatherData(final String template, final ViewGroup input_screen) {
            List<String> fields = getFields(template);
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            input_screen.removeAllViews();
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            for (String field : fields) {
                if (field.contains("|")) {
                    generateData(field);
                }
                EditText item = (EditText) inflater.inflate(R.xml.input_text, null);
                item.setHint(field);
                input_screen.addView(item, layout);
            }
            Button go = new Button(getActivity());
            go.setText(getResources().getString(R.string.generate_story));
            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (validate(input_screen)) {
                        storeData(input_screen);
                        String new_story = substitute(template);
                        ((TextView) storyView.findViewById(R.id.story)).setText(new_story);
                        showStoryView();
                    }
                }
            });
            input_screen.addView(go);
            showInputView();
            String story = template;
            return story;
        }

        private boolean validate(ViewGroup group) {
            // ensure each field is filled in
            int count = group.getChildCount();
            EditText field;
            for (int i=0; i<count; i++) {
                field = (EditText) group.getChildAt(i);
                if (field.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(),"Oops!  You missed one!",Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            return true;
        }

        private void generateData(String field) {
            // for fields that contain '|', just store the value as is.
            if (!field.contains("|")) return;
            prefs.edit().putString(field, field).commit();
        }

        private void storeData(ViewGroup group) {
            // check to see if that key already exists.  If it does not, make a new one, otherwise
            // append the value, separating with '|'
            int count = group.getChildCount();
            EditText field;
            String value;
            for (int i=0; i<count; i++) {
                field = (EditText) group.getChildAt(i);
                value = prefs.getString(field.getHint().toString(), "");
                if (!value.isEmpty()) {
                    value+="|";
                }
                value += field.getText().toString();
                prefs.edit().putString(field.getHint().toString(), value);
            }
        }

        private String substitute(String template) {
            //substitute each value in to the story
            Pattern  pattern = Pattern.compile("\\[([^\\]]*)\\]");
            Matcher matcher = pattern.matcher(template);
            String key, new_word;
            while (matcher.find()) {
                key = matcher.group(1);
                new_word = retrieveWord(key);
                template = template.replaceFirst("\\["+key+"\\]", new_word);
            }
            return template;
        }

        private String retrieveWord(String key) {
            // for keys with multiple values (containing '|'), pick a random value.
            String[] values = prefs.getString(key, "").split("|");
            int index = new Random().nextInt(values.length);
            return values[index];
        }

        private List getFields(String text) {
            List result = new ArrayList();
            Pattern pattern = Pattern.compile("\\[([^\\]]*)\\]");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            return result;
        }

        private void showInputView() {
            storyView.setVisibility(View.GONE);
            inputView.setVisibility(View.VISIBLE);
        }

        private void showStoryView() {
            inputView.setVisibility(View.GONE);
            storyView.setVisibility(View.VISIBLE);
        }
    }

}
