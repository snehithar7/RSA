package com.client;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.security.rsa.RSA;
import com.security.rsa.RSAKey;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONObject;

import javax.swing.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.awt.SystemColor.text;

/**
 *
 * @author alec.ferguson
 */
public class ChatWindowUI extends javax.swing.JFrame {
    // Hardcoded these for now for testing
    private String serverUri="http://127.0.0.1:4000";
    private String userName="test";
    private WebSocketClient client;
    private RSA rsa;
    private RSAKey rsakey;
    private String[] users = new String[]{};
    private Map<String, String> userTextMap = new ConcurrentHashMap<>();
    private Map<String, RSA> userKeyMap = new ConcurrentHashMap<>();

    /**
     * Creates new form ChatWindowUI
     */
    public ChatWindowUI(String userName,
                        String serverUri,
                        RSA rsa,
                        RSAKey rsakey)
    {
        if (serverUri != null)
        {
            this.serverUri = serverUri;
        }

        if (userName != null)
        {
            this.userName = userName;
        }

        this.rsa = rsa;
        this.rsakey = rsakey;
        initComponents();

        try
        {
            client = new WebSocketClient(
                    new URI(this.serverUri + "/ws"),
                    this.userName, rsakey.getBigPrime(),
                    rsakey.getExponent());
        } catch(Exception e){
            e.printStackTrace();
        }
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(10);
        // Poll the user list endpoint every 10 seconds.
        executor.scheduleAtFixedRate(new userListReader(), 0, 10, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new messageReader(), 0, 100, TimeUnit.MILLISECONDS);
    }

    public String inStreamToJson(InputStream in) {
        try {
            BufferedReader streamReader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            return responseStrBuilder.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public class userListReader implements Runnable
    {
        public void run()
        {
            // Make request to server for active user list
            Client client = ClientBuilder.newBuilder().newClient();
            WebTarget target = client.target(serverUri);
            target = target.path("users");

            Invocation.Builder builder = target.request();
            Response response = builder.get();

            // String currentSelected = activeUserList.getSelectedValue();
            // Parse JSON
            InputStream in = response.readEntity(InputStream.class);
            String json = inStreamToJson(in);
            HashMap<String, String> map = new Gson().fromJson(json,
                    new TypeToken<HashMap<String, String>>() {}.getType());

            // Update the keymap
            for (Map.Entry<String, String> entry : map.entrySet())
            {
                String[] s = entry.getValue().split(":");
                String key = entry.getKey();

                // If the key is already in the map, do nothing
                if (userKeyMap.containsKey(key))
                    continue;
                // Otherwise build a new RSA key af
                else
                {
                    RSAKey rsakey = new RSAKey();
                    rsakey.setBigPrime(new BigInteger(s[0]));
                    rsakey.setExponent(new BigInteger(s[1]));
                    userKeyMap.put(key, new RSA(rsakey));
                }
            }

            // Update user array
            String newUsers[] = userKeyMap.keySet().toArray(new String[]{});
            activeUserList.setListData(newUsers);

            // Update selection
            // if (currentSelected != null)
            //    activeUserList.setSelectedValue(currentSelected, true);
            System.out.println("Polling connected users: " + json);
        }
    }

    public class messageReader implements Runnable
    {
        public void run()
        {
            try
            {
                while(client.msgqueue.peek() != null)
                {
                    JSONObject json = new JSONObject(client.msgqueue.remove());
                    String from = json.get("from").toString();
                    String message = json.get("message").toString();

                    // Decrypt message
                    message = rsa.decrypt(message);

                    String currentText = userTextMap.get(from);
                    String newText = (from + ": " + message + "\n");
                    if (currentText == "" || currentText == null)
                    {
                        currentText = newText;
                    }
                    else
                    {
                        currentText += newText;
                    }
                    userTextMap.put(from, currentText);
                    // Show the text if the message is from the selected user
                    if (activeUserList.getSelectedValue() == from)
                        System.out.println("Writing chat");
                        chatTextArea.setText(currentText);
                        chatTextArea.repaint();
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        setTitle("Let's Chat");
        chatTextAreaScrollPane = new javax.swing.JScrollPane();
        chatTextArea = new javax.swing.JTextArea();
        userListLabel = new javax.swing.JLabel();
        activeUserLabel = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        inputTextAreaScrollPane = new javax.swing.JScrollPane();
        inputTextArea = new javax.swing.JTextArea();
        enterButton = new javax.swing.JButton();
        activeUserListScrollPane = new javax.swing.JScrollPane();
        activeUserList = new javax.swing.JList<>(users);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        chatTextArea.setEditable(false);
        chatTextArea.setColumns(20);
        chatTextArea.setLineWrap(true);
        chatTextArea.setRows(5);
        chatTextArea.setText("");
        chatTextAreaScrollPane.setViewportView(chatTextArea);

        userListLabel.setText("Users");

        activeUserLabel.setText("Logged in as");

        usernameTextField.setEditable(false);
        usernameTextField.setToolTipText("");
        usernameTextField.setText(this.userName);

        inputTextArea.setColumns(20);
        inputTextArea.setLineWrap(true);
        inputTextArea.setRows(5);
        inputTextAreaScrollPane.setViewportView(inputTextArea);

        enterButton.setText("Enter");
        enterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterButtonActionPerformed(evt);
            }
        });

        activeUserList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        activeUserList.setToolTipText("");
        activeUserList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                activeUserListValueChanged(evt);
            }
        });
        activeUserList.setListData(users);
        activeUserListScrollPane.setViewportView(activeUserList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(chatTextAreaScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 474, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(activeUserLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(inputTextAreaScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(enterButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(userListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(activeUserListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(userListLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(activeUserLabel)
                                        .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(chatTextAreaScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(inputTextAreaScrollPane)
                                                        .addComponent(enterButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addComponent(activeUserListScrollPane))
                                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void sendMessage(String message,
                             String targetUser)
    {
        // Encrypt message
        message = userKeyMap.get(targetUser).encrypt(message);
        // Build JSON message
        JSONObject json = new JSONObject();
        json.put("from", userName);
        json.put("to", targetUser);
        json.put("message", message);

        try
        {
            client.sendMessage(json.toString());
        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void enterButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (inputTextArea.getText() != ""){
            if (activeUserList.isSelectionEmpty()) {
                // Display a prompt
                chatTextArea.setText("Please select a user to chat with.");
            } else {
                // Update the chat history for this user
                String targetUser = activeUserList.getSelectedValue();
                String currentText = userTextMap.get(targetUser);
                String newText = (this.userName + ": " + inputTextArea.getText() + "\n");
                if (currentText == "" || currentText == null)
                {
                    currentText = newText;
                }
                else
                {
                    currentText += newText;
                }
                // Send message over socket
                sendMessage(inputTextArea.getText(), targetUser);
                userTextMap.put(activeUserList.getSelectedValue(), currentText);
                // Show the text
                chatTextArea.setText(currentText);
                // Clear the input text area
                inputTextArea.setText("");
            }
        }
    }

    private void activeUserListValueChanged(javax.swing.event.ListSelectionEvent evt)
    {
        String currentText = userTextMap.get(activeUserList.getSelectedValue());
        if (activeUserList.isSelectionEmpty())
        {
            chatTextArea.setText("Please select a user to chat with.");
        } else {
            // Update to map value
            if (currentText != null && currentText != chatTextArea.getText()) {
                chatTextArea.setText(userTextMap.get(activeUserList.getSelectedValue()));
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ChatWindowUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChatWindowUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChatWindowUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChatWindowUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                RSAKey testkey = new RSAKey();
                testkey.GenerateKeys();

                RSA testRSA = new RSA(testkey);

                new ChatWindowUI(
                        args[0],
                        null,
                        testRSA,
                        testkey).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JLabel activeUserLabel;
    private javax.swing.JList<String> activeUserList;
    private javax.swing.JScrollPane activeUserListScrollPane;
    private javax.swing.JTextArea chatTextArea;
    private javax.swing.JScrollPane chatTextAreaScrollPane;
    private javax.swing.JButton enterButton;
    private javax.swing.JTextArea inputTextArea;
    private javax.swing.JScrollPane inputTextAreaScrollPane;
    private javax.swing.JLabel userListLabel;
    private javax.swing.JTextField usernameTextField;
    // End of variables declaration
}
