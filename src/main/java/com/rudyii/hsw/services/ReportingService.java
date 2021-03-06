package com.rudyii.hsw.services;

import com.rudyii.hsw.configuration.OptionsService;
import com.rudyii.hsw.helpers.BoardMonitor;
import com.rudyii.hsw.helpers.IpMonitor;
import com.rudyii.hsw.helpers.Uptime;
import com.rudyii.hsw.motion.Camera;
import com.rudyii.hsw.objects.Attachment;
import com.rudyii.hsw.objects.events.CameraRebootEvent;
import com.rudyii.hsw.providers.NotificationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.rudyii.hsw.configuration.OptionsService.*;

@Slf4j
@Service
public class ReportingService {
    private final Uptime uptime;
    private final ArmedStateService armedStateService;
    private final IpMonitor ipMonitor;
    private final IspService ispService;
    private final NotificationsService notificationsService;
    private final BoardMonitor boardMonitor;
    private final OptionsService optionsService;
    private final UuidService uuidService;
    private final List<Camera> cameras;
    private final EventService eventService;

    @Autowired
    public ReportingService(ArmedStateService armedStateService, IspService ispService,
                            NotificationsService notificationsService, IpMonitor ipMonitor,
                            Uptime uptime, BoardMonitor boardMonitor, EventService eventService,
                            OptionsService optionsService, UuidService uuidService,
                            List<Camera> cameras) {
        this.armedStateService = armedStateService;
        this.ispService = ispService;
        this.notificationsService = notificationsService;
        this.ipMonitor = ipMonitor;
        this.uptime = uptime;
        this.boardMonitor = boardMonitor;
        this.eventService = eventService;
        this.optionsService = optionsService;
        this.uuidService = uuidService;
        this.cameras = cameras;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendHourlyReportScheduled() {
        if (armedStateService.isArmed() && (boolean) optionsService.getOption(HOURLY_REPORT_ENABLED) || (boolean) optionsService.getOption(HOURLY_REPORT_FORCED))
            sendHourlyReport();
    }

    @Async
    public void sendHourlyReport() {
        ArrayList<Attachment> attachments = new ArrayList<>();

        cameras.forEach(camera -> {
            try {
                if (camera.getJpegUrl() != null) {
                    ByteArrayOutputStream currentImageBOS = new ByteArrayOutputStream();
                    BufferedImage currentImage = ImageIO.read(new URL(camera.getJpegUrl()));
                    ImageIO.write(currentImage, "jpeg", currentImageBOS);
                    byte[] currentImageByteArray = currentImageBOS.toByteArray();

                    attachments.add(Attachment.builder()
                            .name(camera.getCameraName())
                            .data(currentImageByteArray)
                            .mimeType("image/jpeg").build());
                }
            } catch (Exception e) {
                log.error("Camera " + camera.getCameraName() + " snapshot extraction failed:", e);
                eventService.publish(new CameraRebootEvent(camera.getCameraName()));
            }
        });

        ArrayList<String> body = new ArrayList<>();

        body.add("Home system uptime: <b>" + uptime.getUptime() + "</b>");
        body.add("Current external IP: <b>" + ispService.getCurrentOrLastWanIpAddress() + "</b>");
        body.add("Current internal IP: <b>" + ispService.getLocalIpAddress() + "</b>");
        body.add("Total monitored cameras: <b>" + cameras.size() + "</b>");

        if ((boolean) optionsService.getOption(MONITORING_ENABLED)) {
            body.add("Monitored targets states:");
            body.add("<ul>");
            ipMonitor.getStates().forEach(line -> body.add("<li>" + line));
            body.add("</ul>");

            body.addAll(boardMonitor.getMonitoringResults());
        }

        notificationsService.sendEmail(uuidService.getServerAlias() + " hourly report", body, attachments);
    }
}
