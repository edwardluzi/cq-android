package org.goldenroute.cq;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.goldenroute.cq.model.TestModel;
import org.goldenroute.cq.model.TestSheet;
import org.goldenroute.cq.model.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainFragment extends Fragment implements View.OnClickListener {
    private TestModel mModel = TestModel.Review;

    @Bind(R.id.button_resume)
    protected Button mButtonResume;

    @Bind(R.id.button_restart)
    protected Button mButtonRestart;

    @Bind(R.id.button_select)
    protected Button mButtonSelect;

    @Bind(R.id.text_view_paper)
    protected TextView mTextViewPaper;

    private Map<String, String> mFileMap;
    private String[] mFileList;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(TestModel model) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putInt(TestSheet.ARG_MODEL, TestModel.toInteger(model));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        mButtonResume.setOnClickListener(this);
        mButtonRestart.setOnClickListener(this);
        mButtonSelect.setOnClickListener(this);
        mModel = TestModel.fromInteger(this.getArguments().getInt(TestSheet.ARG_MODEL));
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_resume:
                resume();
                break;
            case R.id.button_restart:
                restart();
                break;
            case R.id.button_select:
                selectTestSheet();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        TestSheet testSheet = TestSheet.load(this.getContext(), mModel, null, false);

        mButtonResume.setEnabled(testSheet != null && testSheet.getTotal() > 0);
        mButtonRestart.setEnabled(testSheet != null && testSheet.getTotal() > 0);

        if (testSheet != null && !TextUtils.isEmpty(testSheet.getTitle())) {
            mTextViewPaper.setText(testSheet.getTitle());
        } else {
            mTextViewPaper.setText(getString(R.string.dialog_title_select_test_sheet));
        }
    }

    private void selectTestSheet() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String storage = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("pref_storage", "0");

        if (!storage.equals("0")) {
            List<String> directories = Utils.getRemovableStorageDirectoryPaths();
            for (int index = 0; index < directories.size(); index++) {
                if (!rootPath.equals(directories.get(index))) {
                    rootPath = directories.get(index);
                    break;
                }
            }
        }

        String folderPath = rootPath + File.separator + "TestSheets";
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            Toast.makeText(this.getActivity(), String.format(getString(R.string.message_folder_not_exists), rootPath), Toast.LENGTH_LONG).show();
            return;
        }

        File files[] = folder.listFiles();

        if (files.length == 0) {
            Toast.makeText(this.getActivity(), String.format(getString(R.string.message_test_sheet_not_found), rootPath), Toast.LENGTH_LONG).show();
            return;
        }


        mFileMap = new HashMap<>();

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                int lastIndex = fileName.lastIndexOf('.');
                if (lastIndex >= 0) {
                    fileName = fileName.substring(0, lastIndex);
                }
                mFileMap.put(fileName, file.getAbsolutePath());
            }
        }

        mFileList = mFileMap.keySet().toArray(new String[mFileMap.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Please select a test sheet");
        builder.setCancelable(false);
        builder.setItems(mFileList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainFragment.this.getActivity(), SheetActivity.class);
                intent.putExtra(TestSheet.ARG_MODEL, TestModel.toInteger(MainFragment.this.mModel));
                intent.putExtra(TestSheet.ARG_PATH, mFileMap.get(mFileList[which]));
                intent.putExtra(TestSheet.ARG_RESTART, false);
                MainFragment.this.getActivity().startActivity(intent);
            }
        });
        builder.show();
    }

    private void resume() {
        Intent intent = new Intent(MainFragment.this.getActivity(), SheetActivity.class);
        intent.putExtra(TestSheet.ARG_MODEL, TestModel.toInteger(MainFragment.this.mModel));
        intent.putExtra(TestSheet.ARG_PATH, "");
        intent.putExtra(TestSheet.ARG_RESTART, false);
        MainFragment.this.getActivity().startActivity(intent);
    }

    private void restart() {
        Intent intent = new Intent(MainFragment.this.getActivity(), SheetActivity.class);
        intent.putExtra(TestSheet.ARG_MODEL, TestModel.toInteger(MainFragment.this.mModel));
        intent.putExtra(TestSheet.ARG_PATH, "");
        intent.putExtra(TestSheet.ARG_RESTART, true);
        MainFragment.this.getActivity().startActivity(intent);
    }
}
