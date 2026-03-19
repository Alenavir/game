package ru.alenavir.gameservice.grpc;

import io.grpc.*;
import org.springframework.stereotype.Component;
import ru.alenavir.gameservice.exceptions.BadRequestException;
import ru.alenavir.gameservice.exceptions.GameException;
import ru.alenavir.gameservice.exceptions.NotFoundException;

@Component
public class GrpcExceptionInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                next.startCall(call, headers)
        ) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (GameException e) {
                    call.close(mapStatus(e), new Metadata());
                } catch (Exception e) {
                    call.close(
                            Status.INTERNAL.withDescription("Internal error"),
                            new Metadata()
                    );
                }
            }
        };
    }

    private Status mapStatus(GameException e) {
        if (e instanceof NotFoundException) {
            return Status.NOT_FOUND.withDescription(e.getMessage());
        }

        if (e instanceof BadRequestException) {
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage());
        }

        return Status.UNKNOWN.withDescription(e.getMessage());
    }
}
