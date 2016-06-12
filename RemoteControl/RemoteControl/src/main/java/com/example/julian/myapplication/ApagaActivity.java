package com.example.julian.myapplication;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jcraft.jsch.*;
import org.w3c.dom.Text;
import android.util.Log;
import android.widget.TimePicker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ApagaActivity extends AppCompatActivity {

    enum TIPO_APAGADO {PROGRAMADO, CANCELADO, AHORA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apaga);
    }

    public int sendCmd(final TIPO_APAGADO tipo_apagado)
    {
        final TimePicker tp = (TimePicker) findViewById(R.id.timePicker2);
        final TextView tv = (TextView) findViewById(R.id.textView2);

        final int[] return_code = {-1};
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    JSch jsch = new JSch();
                    //Change this for your username, IP, and port
                    Session session = jsch.getSession("USER", "ip_address", 22);
                    //Change this for your sudo password
                    session.setPassword("XXXXXXXX");

                    // Avoid asking for key confirmation
                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");
                    session.setConfig(prop);

                    session.connect();

                    // SSH Channel
                    ChannelExec channelssh = (ChannelExec)  session.openChannel("exec");
                    channelssh.setPty(true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    channelssh.setOutputStream(baos);
                    Log.i("info", baos.toString());

                    // Execute command
                    System.out.println("killing shutdowns in execution");
                    //Change this for your command
                    channelssh.setCommand("sudo killall shutdown");
                    InputStream in=channelssh.getInputStream();
                    OutputStream out=channelssh.getOutputStream();
                    ((ChannelExec)channelssh).setErrStream(System.err);

                    channelssh.connect();
                    //Change this for your sudo password
                    out.write(("XXXXXXXXt\n").getBytes());
                    out.flush   ();

                    byte[] tmp=new byte[1024];
                    while(true)
                    {
                        while(in.available()>0){
                            int i=in.read(tmp, 0, 1024);
                            if(i<0)break;
                            System.out.print(new String(tmp, 0, i));
                        }
                        if(channelssh.isClosed()){
                            return_code[0] =  channelssh.getExitStatus();
                            System.out.println("exit-status: "+ return_code[0]);
                            break;
                        }
                    }


                    channelssh.disconnect();


                    if(tipo_apagado == TIPO_APAGADO.CANCELADO)
                    {
                         runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv.setBackgroundColor( (return_code[0]==0)? Color.parseColor("#00FF40") : Color.parseColor("#DF0101"));
                            }
                        });

                        return;
                    }

                    channelssh = (ChannelExec)  session.openChannel("exec");
                    channelssh.setPty(true);
                    baos = new ByteArrayOutputStream();
                    channelssh.setOutputStream(baos);
                    Log.i("info", baos.toString());

                    // Execute command


                    if(tipo_apagado == TIPO_APAGADO.AHORA) {
                        System.out.println("Shutdown NOW!!");
                        channelssh.setCommand("sudo shutdown -P now");
                    }
                    else {
                        System.out.println("Shutdown scheduled at  " + tp.getHour() + ":" + tp.getMinute());
                        channelssh.setCommand("sudo shutdown -P " + tp.getHour() + ":" + tp.getMinute());
                    }

                    System.out.println("getInputStream");
                    in = channelssh.getInputStream();
                    System.out.println("getOutputStream");
                    out = channelssh.getOutputStream();
                    System.out.println("setErrStream");
                    ((ChannelExec)channelssh).setErrStream(System.err);
                    System.out.println("connect");
                    channelssh.connect();
                    System.out.println("getBytes");
                    //Change this for your password
                    out.write(("XXXXXXXX\n").getBytes());
                    System.out.println("flush");
                    out.flush();

                    while(true)
                    {
                        while(in.available()>0){
                            System.out.println("available data");
                            int i=in.read(tmp, 0, 1024);
                            if(i<0){
                                System.out.println("i < 0: " + i);
                                break;
                            }
                            System.out.println(new String(tmp, 0, i));
                        }
                        if(channelssh.isClosed()){
                            System.out.println("channelssh closed");
                            return_code[0] =  channelssh.getExitStatus();
                            System.out.println("exit-status: "+ return_code[0]);
                            break;
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setBackgroundColor( (return_code[0]==0)? Color.parseColor("#00FF40") : Color.parseColor("#DF0101"));
                        }
                    });


                } catch (JSchException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
        return return_code[0];
    }




    public void sendTurnOffNow(View vw) {
        final TextView tv = (TextView) findViewById(R.id.textView2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setBackgroundColor( Color.parseColor("#FFFFFF"));
            }
        });
        tv.setText("Apagando ahora");
        sendCmd(TIPO_APAGADO.AHORA);
    }

    public void sendTurnOffTime(View vw) {
        final TextView tv =   (TextView)   findViewById(R.id.textView2);
        final TimePicker tp = (TimePicker) findViewById(R.id.timePicker2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setBackgroundColor( Color.parseColor("#FFFFFF"));
            }
        });
        tv.setText("Apagando Programado " + tp.getHour() + ":" + tp.getMinute() );
        sendCmd(TIPO_APAGADO.PROGRAMADO);
    }

    public void cancelTurnOff(View vw) {
        final TextView tv = (TextView) findViewById(R.id.textView2);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setBackgroundColor( Color.parseColor("#FFFFFF"));
            }
        });
        tv.setText("Apagado cancelado");
        sendCmd(TIPO_APAGADO.CANCELADO);
    }

}