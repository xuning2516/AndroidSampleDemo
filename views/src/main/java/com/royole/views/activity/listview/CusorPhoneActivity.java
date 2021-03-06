package com.royole.views.activity.listview;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.royole.androidcommon.utils.PermissionUtils;
import com.royole.views.R;

import java.util.Arrays;

public class CusorPhoneActivity extends AppCompatActivity {
    private static final String TAG = "zhanghao";
    
    private ListView mListView;
    private Context mContext;
    private static final  int REQUEST_CODE_PERMISSIONS = 0x002;
    private final String[] PERMISSIONS = new String[]{Manifest.permission.READ_CONTACTS
            , Manifest.permission.WRITE_CONTACTS};

    private static final String[] PHONE_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private static final int COLUMN_TYPE = 1;;
    private static final int COLUMN_LABEL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cusor_phone);
        mListView = findViewById(R.id.list);
        mContext = this;
        initCheckPermissions();
    }


    private void initCheckPermissions(){
        if(Build.VERSION.SDK_INT >= 23) { //android M only
            PermissionUtils.checkPermissions(mContext,
                    new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},
                    new PermissionUtils.PermissionCheckCallBack() {
                        @Override
                        public void onGranted() {
                            //onHasPermissiontoCamera();
                            initDataAndView();
                        }

                        @Override
                        public void onDenied(final String... permission) {
                            showExplainDialog(permission, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PermissionUtils.requestPermissions(mContext, permission, REQUEST_CODE_PERMISSIONS);
                                }
                            });
                        }

                        @Override
                        public void onDeniedDontAsk(String... permission) {
                            PermissionUtils.requestPermissions(mContext, permission, REQUEST_CODE_PERMISSIONS);
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        PermissionUtils.onRequestPermissionsResult(mContext, PERMISSIONS, grantResults, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onGranted() {
                // toCamera();
                initDataAndView();
            }

            @Override
            public void onDenied(String... permission) {
                Toast.makeText(mContext, "我们需要"+Arrays.toString(permission)+"权限", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeniedDontAsk(String... permission) {
                Toast.makeText(mContext, "我们需要"+Arrays.toString(permission)+"权限", Toast.LENGTH_SHORT).show();
                PermissionUtils.toAppSetting(mContext);
            }
        });
    }

    /**
     * 解释权限的dialog
     *
     */
    private void showExplainDialog(String[] permission, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(mContext)
                .setTitle("申请权限")
                .setMessage("我们需要" + Arrays.toString(permission)+"权限")
                .setPositiveButton("确定", onClickListener)
                .show();
    }

    private void initDataAndView(){
        // Get a cursor with all phones
        Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PHONE_PROJECTION, null, null, null);
        startManagingCursor(c);

        // Map Cursor columns to views defined in simple_list_item_2.xml
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                new int[] { android.R.id.text1, android.R.id.text2 },
                CursorAdapter.FLAG_AUTO_REQUERY);
        //Used to display a readable string for the phone type
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                Log.d(TAG, "setViewValue: "+ cursor.getString(columnIndex));
               // Log.d(TAG, "setViewValue: ");
                //Let the adapter handle the binding if the column is not TYPE
                if (columnIndex != COLUMN_TYPE) {
                    return false;
                }
                int type = cursor.getInt(COLUMN_TYPE);
                String label = null;
                //Custom type? Then get the custom label
                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                    label = cursor.getString(COLUMN_LABEL);
                }
                //Get the readable string
                String text = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, label);
                //Set text
                ((TextView) view).setText(text);
                return true;
            }
        });
        mListView.setAdapter(adapter);
    }
}
