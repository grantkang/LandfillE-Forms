package com.landfilleforms.android.landfille_forms;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.landfilleforms.android.landfille_forms.database.LandFillBaseHelper;
import com.landfilleforms.android.landfille_forms.database.LandFillDbSchema;
import com.landfilleforms.android.landfille_forms.database.dao.ImeDao;
import com.landfilleforms.android.landfille_forms.database.dao.InstantaneousDao;
import com.landfilleforms.android.landfille_forms.model.DataDump;
import com.landfilleforms.android.landfille_forms.model.ImeData;
import com.landfilleforms.android.landfille_forms.model.InstantaneousData;
import com.landfilleforms.android.landfille_forms.model.User;
import com.landfilleforms.android.landfille_forms.model.WarmSpotData;
import com.landfilleforms.android.landfille_forms.database.dao.WarmSpotDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



/**
 * Created by owchr on 2/10/2017.
 */

public class ExportFragment extends Fragment {
    Button mExport;
    private SessionManager session;
    private User mUser;
    private SQLiteDatabase mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);


        session = new SessionManager(getActivity().getApplicationContext());
        session.checkLogin();

        HashMap<String,String> currentUser = session.getUserDetails();
        mUser = new User();
        mUser.setUsername(currentUser.get(SessionManager.KEY_USERNAME));
        mUser.setFullName(currentUser.get(SessionManager.KEY_USERFULLNAME));
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_export, container, false);
        getActivity().setTitle("Export");
        mExport = (Button) view.findViewById(R.id.menu_export_button);
        mExport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Context context = getActivity();

                InstantaneousDao instantaneousDao = InstantaneousDao.get(getActivity());
                List<InstantaneousData> instantaneousDatas = instantaneousDao.getInstantaneousDatas();

                ImeDao imeDao = ImeDao.get(getActivity());
                List<ImeData> imeDatas = imeDao.getImeDatas();

                WarmSpotDao warmSpotDao = WarmSpotDao.get(getActivity());
                List<WarmSpotData> warmSpotDatas = warmSpotDao.getWarmSpotDatas();

                DataDump dataDump = new DataDump(instantaneousDatas, imeDatas, warmSpotDatas);

                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                builder.setDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                Gson gson = builder.create();

                String jsonOutput = gson.toJson(dataDump);

                Log.d("Json",jsonOutput);
                int messageResId;

                Date d = new Date();
                String name = "LandFillDataExport" + d.toString() + ".json";
                String path = "LandfillData";
                try {

                    File myFile = new File(Environment.getExternalStorageDirectory()+File.separator+path);
                    myFile.mkdirs();
                    myFile = new File(Environment.getExternalStorageDirectory()+File.separator+path+File.separator+name);

                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(jsonOutput);

                    myOutWriter.close();
                    fOut.close();
                    messageResId = R.string.export_successful_toast;
                    Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
                    mDatabase = new LandFillBaseHelper(getActivity()).getWritableDatabase();
                    mDatabase.execSQL("delete from "+ LandFillDbSchema.InstantaneousDataTable.NAME);
                    mDatabase.execSQL("delete from "+ LandFillDbSchema.ImeDataTable.NAME);
                    mDatabase.execSQL("delete from "+ LandFillDbSchema.WarmSpotDataTable.NAME);


                    //TODO: Create a way for the (Instantaneous/IME/Warmspot) Data to get either wiped out or be hidden.

                } catch (IOException e) {
                    e.printStackTrace();
                    messageResId = R.string.export_unsuccessful_toast;
                    Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show();
                }

            }
        });
        return view;
    }
}
