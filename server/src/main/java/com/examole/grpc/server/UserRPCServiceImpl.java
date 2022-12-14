package com.examole.grpc.server;


import com.example.grpc.bank.BankCardProto;
import com.example.grpc.dto.UserProto;
import com.example.grpc.enums.SexEnumProto;
import com.example.grpc.rpc.UserRPCProto;
import com.example.grpc.rpc.UserRPCServiceGrpc;
import com.github.javafaker.Faker;
import io.grpc.stub.StreamObserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class UserRPCServiceImpl extends UserRPCServiceGrpc.UserRPCServiceImplBase{
    private final static Integer MAX_USERS = 10;
    Faker faker = new Faker();
    @Override
    public void listUsers(UserRPCProto.Request request, StreamObserver<UserRPCProto.Response> responseObserver) {
        System.out.println("Server receive request.");

        for (int i = 0; i < MAX_USERS; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String fullName = getFullName();
            UserRPCProto.Response response = UserRPCProto.Response.newBuilder()
                    .setCode(i)
                    .setMsg("success")
                    .addUser(UserProto.User.newBuilder()
                            .setName(fullName)
                            .setAge(getAge())
                            .setSex(getSexEnum())
                            .setBankCard(getBankCard())
                            .setEmail(getEmail(fullName)))
                            .build();

            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    public int getAge() {
        return faker.number().numberBetween(18, 100);
    }

    public String getEmail(String fullName) {
        String result = Arrays.stream(fullName.split(" "))
                .map(e -> e.toLowerCase())
                .reduce((a, b) -> a + b).get();
        return result + "@gmail.com";
    }

    public String getFullName() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    public SexEnumProto.SexEnum getSexEnum() {
        int randomNumber = faker.number().numberBetween(0, 3);
        return randomNumber == 1 ? SexEnumProto.SexEnum.MALE : SexEnumProto.SexEnum.FEMALE;
    }

    public BankCardProto.BankCard getBankCard() {
        return BankCardProto.BankCard.newBuilder()
                .setCardExpiryDate(LocalDateTime.now().plusDays(faker.number().numberBetween(30, 180)).format(DateTimeFormatter.ofPattern("MM/yy")))
                .setNumberCard(faker.business().creditCardNumber())
                .setBalance(faker.number().randomDouble(2, 0, 9999))
                .build();
    }
}
