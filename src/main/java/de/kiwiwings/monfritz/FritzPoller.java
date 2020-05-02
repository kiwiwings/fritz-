/*
 * Copyright (c) 2017 by Andreas Beeker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kiwiwings.monfritz;

import eu.hansolo.fx.smoothcharts.SmoothedChart;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class FritzPoller extends Application {

    enum Series {
        sent, recv, sentMax, recvMax
    }

    private final List<ScheduledService<?>> svcList = new ArrayList<>();

    private final long[] data = new long[Series.values().length];

    private SmoothedChart<String, Number> chart;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private static final int dataLimit = 20;
    private static final double dataIntervalInSec = 1;

    @Override
    public void init() {
        ScheduledService<GetAddonInfosResponse> transferSvc = new ScheduledService<>() {
            protected Task<GetAddonInfosResponse> createTask() {
                return FritzPollerTask.getAddonInfos();
            }
        };
        transferSvc.setPeriod(Duration.seconds(dataIntervalInSec));
        transferSvc.setOnSucceeded(this::addTransferData);
        svcList.add(transferSvc);

        ScheduledService<GetCommonLinkPropertiesResponse> limitsSvc = new ScheduledService<>() {
            protected Task<GetCommonLinkPropertiesResponse> createTask() {
                return FritzPollerTask.getCommonLinkProperties();
            }
        };
        limitsSvc.setPeriod(Duration.seconds(10));
        limitsSvc.setOnSucceeded(this::addLimitsData);
        svcList.add(limitsSvc);

        ScheduledService<Date> triggerSvc = new ScheduledService<>() {
            protected Task<Date> createTask() {
                return new Task<>() {
                    @Override
                    protected Date call() {
                        return new Date();
                    }
                };
            }
        };
        triggerSvc.setPeriod(Duration.seconds(1));
        triggerSvc.setOnSucceeded(this::triggerChart);
        svcList.add(triggerSvc);


        final NumberAxis na = new NumberAxis();
        na.setLabel("bytes/sec");
        final CategoryAxis ca = new CategoryAxis();
        ca.setLabel("time");
        ca.setTickLabelRotation(45);

        chart = new SmoothedChart<>(ca, na);
        chart.setAnimated(false);
        chart.setSmoothed(true);
        chart.setChartType(SmoothedChart.ChartType.AREA);
        chart.setChartPlotBackground(Color.rgb(31, 31, 31));

        for (Series s : Series.values()) {
            final XYChart.Series<String, Number> xys = new XYChart.Series<>(
                    s.name(), FXCollections.observableList(new LinkedList<>()));
            chart.getData().add(xys);
            chart.getStrokePath(xys).setStrokeWidth(3);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        svcList.forEach(Service::start);

        primaryStage.setTitle("Mon-Fritz transfer stats");

        StackPane root = new StackPane();
        final ObservableList<Node> childList = root.getChildren();
        childList.add(chart);
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();

    }

    private void addTransferData(final WorkerStateEvent evt) {
        GetAddonInfosResponse response = (GetAddonInfosResponse) evt.getSource().getValue();
        if (response.byteSendRate != null) {
            data[Series.recv.ordinal()] = response.byteReceiveRate;
            data[Series.sent.ordinal()] = response.byteSendRate;
        }
    }

    private void addLimitsData(final WorkerStateEvent evt) {
        GetCommonLinkPropertiesResponse response = (GetCommonLinkPropertiesResponse) evt.getSource().getValue();
        if (response.layer1DownstreamMaxBitRate != null) {
            data[Series.recvMax.ordinal()] = response.layer1DownstreamMaxBitRate/8;
            data[Series.sentMax.ordinal()] = response.layer1UpstreamMaxBitRate/8;
        }
    }

    private void triggerChart(final WorkerStateEvent evt) {
        final String label = dateFormat.format((Date)evt.getSource().getValue());

        int dataIdx = 0;
        for (XYChart.Series<String, Number> series : chart.getData()) {
            final ObservableList<XYChart.Data<String, Number>> seriesData = series.getData();

            if (seriesData.size() == dataLimit) {
                seriesData.remove(0);
            }

            seriesData.add(new XYChart.Data<>(label, data[dataIdx++]));
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
