package com.tankbell.naloxone;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

/**
 * Copyright 2016. TankBell Technologies.
 * All Rights Reserved .
 */

public interface MyInterface {
    /**
     * Invoke the Lambda function "AndroidBackendLambdaFunction".
     * The function name is the method name.
     */
    @LambdaFunction
    String AndroidBackendLambdaFunction(RequestClass request);
    //String AndroidBackendLambdaFunction(RequestClass request);
}
