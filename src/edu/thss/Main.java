package edu.thss;

import edu.thss.tcp.FileTransferClient;
import edu.thss.tcp.FileTransferServer;

/**
 * Created by wuwe on 11/18/2015.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Missing arguments: [Server] or [Client]");
            System.out.println();
            System.exit(-1);
        }

        if ("Server".equalsIgnoreCase(args[0])) {
            FileTransferServer.main(args);
        } else if ("Client".equalsIgnoreCase(args[0])){
            FileTransferClient.main(args);
        } else {
            System.out.println("Invalid arguments '" + args[0] + "'");
            System.out.println("[Server] or [Client]");
            System.exit(-1);
        }
    }
}