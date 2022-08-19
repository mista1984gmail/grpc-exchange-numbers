package com.deft.grpc.client;

import com.deft.grpc.rpc.UserRPCProto;
import com.deft.grpc.rpc.UserRPCServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserGRPCClient {
    private static final Logger logger = LoggerFactory.getLogger(UserGRPCClient.class.getName());

    private final ManagedChannel channel;
    private final UserRPCServiceGrpc.UserRPCServiceBlockingStub blockingStub;

    public static void main(String[] args) throws Exception {
        UserGRPCClient client = new UserGRPCClient("localhost", 3333);
        try {
            client.request();
        } finally {
            client.shutdown();
        }
    }

    public UserGRPCClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    public UserGRPCClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = UserRPCServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void request() {
        List<UserRPCProto.Response>list=new ArrayList<>();
        UserRPCProto.Request request = UserRPCProto.Request.newBuilder().build();
        Iterator<UserRPCProto.Response> response = blockingStub.listUsers(request);
        while (response.hasNext()) {
            list.add(response.next());
        }
        list.stream()
                .map(e -> e.getUser(0))
                .forEach(System.out::println);

        for (int i = 0; i < list.size(); i++) {
            LocalDate expiryDate = parseDate(list.get(i).getUser(0).getBankCard().getCardExpiryDate());
            LocalDate dateLessThanThreeMonths = LocalDate.now().plusDays(90);
            if(expiryDate.isBefore(dateLessThanThreeMonths)){
                logger.info("send email to: {}", list.get(i).getUser(0).getEmail());
                logger.info("Dear, {} ! Your card is expiring: {}. Contact your bank to extend the expiration date.",
                        list.get(i).getUser(0).getName(),
                        list.get(i).getUser(0).getBankCard().getCardExpiryDate());
            }
        }
        channel.shutdownNow();

    }
    public LocalDate parseDate(String date){
        return LocalDate.of(2000 + parseStringToInt(date.substring(3,5)),
                        parseStringToInt(date.substring(0,2)), 1)
                .plusMonths(1);
    }

    public int parseStringToInt(String str){
        return str.startsWith("0") ? Integer.parseInt(str.substring(1)) : Integer.parseInt(str);
    }


}
