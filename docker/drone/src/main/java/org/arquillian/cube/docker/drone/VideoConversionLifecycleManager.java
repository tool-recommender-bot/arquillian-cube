package org.arquillian.cube.docker.drone;

import org.arquillian.cube.docker.drone.event.AfterConversion;
import org.arquillian.cube.spi.Cube;
import org.arquillian.cube.spi.CubeControlException;
import org.arquillian.cube.spi.CubeRegistry;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoConversionLifecycleManager {

    Cube flv2mp4;

    @Inject
    Event<AfterConversion> afterConversionEvent;

    @Inject
    Instance<SeleniumContainers> seleniumContainersInstance;

    public void startConversion(@Observes AfterSuite afterSuite, CubeDroneConfiguration cubeDroneConfiguration,
        CubeRegistry cubeRegistry) {

        try {
            if (cubeDroneConfiguration.isRecording()) {
                initConversionCube(cubeRegistry);
                flv2mp4.create();
                flv2mp4.start();

                afterConversionEvent.fire(new AfterConversion());
            }
        } catch (CubeControlException e) {
            Logger.getLogger(VideoConversionLifecycleManager.class.getName()).log(Level.WARNING, "Failed to start flv2mp4", e);
        }
    }

    private void initConversionCube(CubeRegistry cubeRegistry) {
        if (flv2mp4 == null) {
            SeleniumContainers seleniumContainers = seleniumContainersInstance.get();
            
            Cube conversionContainer = cubeRegistry.getCube(seleniumContainers.getVideoConverterContainerName());

            if (conversionContainer == null) {
                throw new IllegalArgumentException(
                        "Video conversion cube is not present in the registry.");
            }

            this.flv2mp4 = conversionContainer;
        }
    }

    public void stopContainer(@Observes AfterConversion afterConversion) {

        if (this.flv2mp4 != null) {
            try {
                flv2mp4.stop();
                flv2mp4.destroy();
            } catch (CubeControlException e) {
                Logger.getLogger(VideoConversionLifecycleManager.class.getName()).log(Level.WARNING, "Failed to stop flv2mp4", e);
            }
        }
    }
}
