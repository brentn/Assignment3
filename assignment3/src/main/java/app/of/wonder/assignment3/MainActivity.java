package app.of.wonder.assignment3;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends ActionBarActivity {

// This app allows you to create your own story from a template (in the strings file).
// The template can surround fields, denoted by square brackets, for which the user will provide values.
// It can also provide "this or that" choices formatted as a field with pipes: ie. [this|that|the other]
// for which the computer will provide the value (random)
// finally, to re-use a field, prefix it with "+", so that [+Girls name] will be replaced with the whatever
// the first value of [Girls name] was throughout the story.  (note that quotes must be preceeded by backslashes)



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


    public static class PlaceholderFragment extends Fragment {

        private static final String TAG = "MainActivity";
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
            gatherData(getResources().getString(R.string.template_1), input_fields);
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
                } else {
                    EditText item = (EditText) inflater.inflate(R.xml.input_text, null);
                    item.setContentDescription(field);
                    item.setHint(field);
                    input_screen.addView(item, layout);
                }
            }
            Button go = new Button(getActivity());
            go.setPadding(5,10,5,20);
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
            return template;
        }

        private boolean validate(ViewGroup group) {
            // ensure each field is filled in
            int count = group.getChildCount();
            EditText field;
            for (int i=0; i<count; i++) {
                if (group.getChildAt(i) instanceof EditText) {
                    field = (EditText) group.getChildAt(i);
                    if (prefs.getString(field.getContentDescription().toString(),"").isEmpty()) {
                        if (field.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(),"Oops!  You need to provide a "
                                    +field.getContentDescription().toString(),Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private void generateData(String field) {
            // for fields that contain '|', just store the value as is.
            if (!field.contains("|")) return;
            prefs.edit().putString(field, field).commit();
            Log.d(TAG,field);
        }

        private void storeData(ViewGroup group) {
            // check to see if that key already exists.  If it does not, make a new one, otherwise
            // append the value, separating with '|'
            int count = group.getChildCount();
            EditText field;
            String value;
            for (int i=0; i<count; i++) {
                if (group.getChildAt(i) instanceof EditText) {
                    field = (EditText) group.getChildAt(i);
                    value = prefs.getString(field.getHint().toString(), "");
                    if (!value.isEmpty() && !field.getText().toString().isEmpty()) {
                        value+="|";
                    }
                    value += field.getText().toString();
                    prefs.edit().putString(field.getContentDescription().toString(), value).commit();
                    Log.d(TAG, field.getContentDescription().toString()+":"+value);
                }
            }
        }

        private String substitute(String template) {
            //substitute each value in to the story
            Pattern  pattern = Pattern.compile("\\[([^\\+\\]]*)\\]");
            Matcher matcher = pattern.matcher(template);
            String key, new_word;
            while (matcher.find()) {
                key = matcher.group(1);
                new_word = retrieveWord(key);
                key = key.replaceAll("\\|","\\\\\\|");
                template = template.replaceFirst("\\["+key+"\\]", new_word);
                template = template.replaceAll("\\[\\+"+key+"\\]", new_word);
                Log.d(TAG, "key:"+key);
                Log.d(TAG, "more:"+"\\[\\+"+key+"\\]");
            }
            return template;
        }

        private String retrieveWord(String key) {
            // for keys with multiple values (containing '|'), pick a random value.
            String[] values = prefs.getString(key, "").split("\\|");
            int index = new Random().nextInt(values.length);
            String result = values[index];
            Log.d(TAG, values[index]+":"+result);
            return result;
        }

        private List<String> getFields(String text) {
            List<String> result = new ArrayList<String>();
            Pattern pattern = Pattern.compile("\\[([^\\+\\]]*)\\]");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }
            // sorting will somewhat randomize the fields, as well as grouping related fields together.
            Collections.sort(result);
            return result;
        }

        private void showInputView() {
            storyView.setVisibility(View.GONE);
            inputView.setVisibility(View.VISIBLE);
        }

        private void showStoryView() {
            inputView.setVisibility(View.GONE);
            storyView.setVisibility(View.VISIBLE);
            storyView.requestFocus();
        }
    }

}
