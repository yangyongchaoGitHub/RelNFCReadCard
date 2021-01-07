package com.dataexpo.nfcsample.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class NfcUtils {
    private static NfcAdapter mNfcAdapter;

    private static PendingIntent mPendingIntent;
    private static IntentFilter[] mIntentFilters;
    private static String[][] mTechLists;
    private final static String ACTION_NAME = "android.nfc.action.TECH_DISCOVERED";

    /**
     * 检查NFC是否打开
     */
    public static void NfcCheck(Context context) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);

        if (mNfcAdapter == null) {
            Toast.makeText(context, "设备不支持NFC功能!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                IsToSet(context);
            } else {
                //Toast.makeText(context, "NFC功能已打开!", Toast.LENGTH_SHORT).show();
            }
        }
        NfcInit(context);
    }

    /**
     * 初始化nfc设置
     */
    public static void NfcInit(Context context) {
        Intent mIntent = new Intent (ACTION_NAME);
        mPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter techFilter = new IntentFilter (NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            techFilter.addDataType ("text/plain");
            mIntentFilters = new IntentFilter[]{techFilter};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mTechLists  = new String[][] {
                new String[] {"android.nfc.tech.NfcA"},
                new String[]{"android.nfc.tech.NfcB"},
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.NfcV"},
                new String[]{"android.nfc.tech.Ndef"},
                new String[]{"android.nfc.tech.NdefFormatable"},
                new String[]{"android.nfc.tech.IsoDep"},
                new String[]{"android.nfc.tech.MifareClassic"},
                new String[]{"android.nfc.tech.MifareUltralight"}
        };
    }

    public static void enable(Activity activity) {
        mNfcAdapter.enableForegroundDispatch(activity, mPendingIntent, mIntentFilters, mTechLists);
    }

    public static void disable(Activity activity) {
        mNfcAdapter.disableForegroundDispatch(activity);
    }

    /**
     * 读取NFC的数据
     */
    public static String readNFCFromTag(Intent intent) throws UnsupportedEncodingException {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            if (mNdefRecord != null) {
                String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                return readResult;
            }
        }
        return "";
    }


    /**
     * 往nfc写入数据
     */
    public static void writeNFCToTag(String data, Intent intent) throws IOException, FormatException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefRecord ndefRecord = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ndefRecord = NdefRecord.createTextRecord(null, data);
        }
        NdefRecord[] records = {ndefRecord};
        NdefMessage ndefMessage = new NdefMessage(records);
        ndef.writeNdefMessage(ndefMessage);
    }

    /**
     * 读取nfcID
     */
    public static String readNFCId(Intent intent) throws UnsupportedEncodingException {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String id = ByteArrayToHexString(tag.getId());
        return id;
    }

    /**
     * 将字节数组转换为字符串
     */
    private static String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private static void IsToSet(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("是否跳转到设置页面打开NFC功能");
//        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goToSet(context);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private static void goToSet(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// 运行系统在5.x环境使用
            // 进入设置系统应用权限界面
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
            return;
        }
    }

    public static String getCardId(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String CardId = bytesToHexStringNo0x(tagFromIntent.getId());
        long cardId = Long.parseLong(CardId, 16);
        CardId = Long.toString(cardId);
        if (CardId.length() < 10) {
            StringBuilder add = new StringBuilder();
            for (int i = 0; i < 10- CardId.length(); i++) {
                add.insert(0, "0");
            }
            CardId = add + CardId;
        }
        return CardId;
    }

    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder ("0x");
        if (src == null || src.length <= 0) { return null; }
        char[] buffer = new char[2];
        for ( int i = 0 ; i < src.length ; i++ ) {
            buffer[0] = Character.forDigit ((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit (src[i] & 0x0F, 16);
            //System.out.println (buffer);
            stringBuilder.append (buffer);
        }
        return stringBuilder.toString();
    }

    private static String bytesToHexStringNo0x(byte[] src){
        StringBuilder stringBuilder = new StringBuilder ();
        if (src == null || src.length <= 0) { return null; }
        char[] buffer = new char[2];
        for ( int i = 0 ; i < src.length ; i++ ) {
            buffer[0] = Character.forDigit ((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit (src[i] & 0x0F, 16);
            //System.out.println (buffer[0] + " i" + i + " " +buffer[1]);
            stringBuilder.append (buffer);
        }
        return stringBuilder.toString();
    }

    private void processIntent(Intent intent){
        // 取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        for ( String tech : tagFromIntent.getTechList () ) {
//            System.out.println (tech);
//        }
        boolean auth = false;
        // 读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        //String metaInfo = "本标签的UID为" + Coverter.getUid (intent) + "\n";
        String CardId =bytesToHexStringNo0x(tagFromIntent.getId());
        Log.i("0000000000 卡号是：--", CardId + "");

        Log.i("什么是F", 0xff + "");

        long cardId = 0l;
        byte[] id16 = tagFromIntent.getId();
        Log.i("0: ", id16[0] + " " + (id16[0] << 24));
        Log.i("id16 ", id16.length + "");
        Log.i("1: ", id16[1] + "" + (id16[1] << 16));
        cardId = cardId | ((id16[0] << 24));
        cardId = cardId | ((id16[1] << 16) & 0x00ff0000);
        cardId = cardId | ((id16[2] << 8) & 0x0000ff00);
        cardId = cardId | ((id16[3]) & 0xff);

        Log.i("0000000000 卡号是", CardId + " >> 十进制 " + cardId);
        cardId = Long.parseLong(CardId, 16);
        Log.i("111 卡号是", CardId + " >> 十进制 " + cardId);
        String metaInfo = "本标签的UID为" + CardId + "\n";

        if (mfc != null) {
            try {
                // Enable I/O operations to the tag from this TagTechnology
                // object.
                mfc.connect();
                int type = mfc.getType ();// 获取TAG的类型
                int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
                String typeS = "";
                switch (type) {
                    case MifareClassic.TYPE_CLASSIC:
                        typeS = "TYPE_CLASSIC";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        typeS = "TYPE_PLUS";
                        break;
                    case MifareClassic.TYPE_PRO:
                        typeS = "TYPE_PRO";
                        break;
                    case MifareClassic.TYPE_UNKNOWN:
                        typeS = "TYPE_UNKNOWN";
                        break;
                }

                Log.i("==========", "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount () + "个块\n存储空间: " + mfc.getSize () + "B\n");

                metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount () + "个块\n存储空间: " + mfc.getSize () + "B\n";
//                for ( int j = 0 ; j < sectorCount ; j++ ) {
//                    // Authenticate a sector with key A.
//                    auth = mfc.authenticateSectorWithKeyA (j, MifareClassic.KEY_DEFAULT);
//                    int bCount;
//                    int bIndex;
//                    if (auth) {
//                        metaInfo += "Sector " + j + ":验证成功\n";
//                        // 读取扇区中的块
//                        bCount = mfc.getBlockCountInSector (j);
//                        bIndex = mfc.sectorToBlock (j);
//                        for ( int i = 0 ; i < bCount ; i++ ) {
//                            byte[] data = mfc.readBlock (bIndex);
//                            metaInfo += "Block " + bIndex + " : " + bytesToHexString(data) + "\n";
//                            bIndex++;
//                        }
//                    } else {
//                        metaInfo += "Sector " + j + ":验证失败\n";
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
        Log.i("--------------", metaInfo);
    }
}
