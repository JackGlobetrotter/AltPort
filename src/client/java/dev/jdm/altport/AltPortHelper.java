package dev.jdm.altport;

import net.minecraft.client.network.ServerInfo;
import java.lang.reflect.Field;
import java.util.Arrays;

public class AltPortHelper {
    public static int getFailoverPort(ServerInfo server){
        try {
            if (Arrays.stream(server.getClass().getFields()).anyMatch(f -> f.getName().equals("failoverPort"))) {
                Field field = server.getClass().getDeclaredField("failoverPort");
                return (Integer) field.get(server);
            }
        }
        catch(Exception ex){System.out.println("Failed to get Failover port");}

        return  -1;
    }

    public static void setFailoverPort(ServerInfo server, int failoverPort){
        try {
            Field field = server.getClass().getDeclaredField("failoverPort");
            field.set(server,failoverPort);
        }
        catch(Exception ex){
            System.out.println("Failed to save failover Port : "+ ex.getMessage());
        }
    }
}
