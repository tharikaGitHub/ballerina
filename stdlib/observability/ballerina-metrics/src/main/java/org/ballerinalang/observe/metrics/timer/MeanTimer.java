/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe.metrics.timer;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BEnumerator;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.metrics.Tag;
import org.ballerinalang.util.metrics.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Returns the distribution average for all recorded events.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "metrics",
        functionName = "mean",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Timer",
                structPackage = "ballerina.metrics"),
        args = {@Argument(name = "timer", type = TypeKind.STRUCT, structType = "Timer",
                structPackage = "ballerina.metrics"), @Argument(name = "timeUnit", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.FLOAT)},
        isPublic = true
)
public class MeanTimer extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        BStruct timerStruct = (BStruct) context.getRefArgument(0);
        String name = timerStruct.getStringField(0);
        String description = timerStruct.getStringField(1);
        BMap tagsMap = (BMap) timerStruct.getRefField(0);
        BEnumerator timeUnitEnum = (BEnumerator) context.getRefArgument(1);

        TimeUnit timeUnit = TimeUnitExtractor.getTimeUnit(timeUnitEnum);

        if (!tagsMap.isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for (Object key : tagsMap.keySet()) {
                tags.add(new Tag(key.toString(), tagsMap.get(key).stringValue()));
            }
            context.setReturnValues(new BFloat(Timer.builder(name).description(description).tags(tags).register()
                    .mean(timeUnit)));

        } else {
            context.setReturnValues(new BFloat(Timer.builder(name).description(description).register().mean(timeUnit)));
        }
    }
}
