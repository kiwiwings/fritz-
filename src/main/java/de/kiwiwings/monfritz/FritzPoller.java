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
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class FritzPoller extends Application {

    private ScheduledService<GetAddonInfosResponse> svc;
    private XYChart.Series<String, Number> sentBytesSeries;
    private XYChart.Series<String, Number> recvBytesSeries;
    private SimpleDateFormat dateFormat;
    private static final int dataLimit = 20;
    private static final double dataIntervalInSec = 1;

    @Override
    public void init() {
        svc = new ScheduledService<>() {
            protected Task<GetAddonInfosResponse> createTask() {
                return new FritzPollerTask();
            }
        };
        svc.setPeriod(Duration.seconds(dataIntervalInSec));
        svc.setOnSucceeded(this::addDataToSeries);
        sentBytesSeries = new XYChart.Series<>("sent", FXCollections.observableList(new LinkedList<>()));
        recvBytesSeries = new XYChart.Series<>("recv", FXCollections.observableList(new LinkedList<>()));
        dateFormat = new SimpleDateFormat("HH:mm:ss");
    }

    @Override
    public void start(Stage primaryStage) {
        svc.start();

        primaryStage.setTitle("Mon-Fritz transfer stats");

        final NumberAxis na = new NumberAxis();
        na.setLabel("bytes/sec");
        final CategoryAxis ca = new CategoryAxis();
        ca.setLabel("time");
        ca.setTickLabelRotation(45  );

        SmoothedChart<String, Number> chart = new SmoothedChart<>(ca, na);
        chart.getData().add(sentBytesSeries);
        chart.getData().add(recvBytesSeries);
        chart.setAnimated(false);
        chart.setSmoothed(true);
        chart.setChartType(SmoothedChart.ChartType.AREA);
        chart.setChartPlotBackground(Color.rgb(31, 31, 31));
        chart.getStrokePath(sentBytesSeries).setStrokeWidth(3);
        chart.getStrokePath(recvBytesSeries).setStrokeWidth(3);


        StackPane root = new StackPane();
        final ObservableList<Node> childList = root.getChildren();
        childList.add(chart);
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();

    }

    private void addDataToSeries(final WorkerStateEvent evt) {
        GetAddonInfosResponse response = (GetAddonInfosResponse) evt.getSource().getValue();
        if (response.byteSendRate == null) {
            return;
        }


        ObservableList<XYChart.Data<String, Number>> sentData = sentBytesSeries.getData();
        ObservableList<XYChart.Data<String, Number>> recvData = recvBytesSeries.getData();

        if (sentData.size() == dataLimit) {
            sentData.remove(0);
            recvData.remove(0);
        }


        String date = dateFormat.format(new Date());
        sentData.add(new XYChart.Data<>(date, response.byteSendRate));
        recvData.add(new XYChart.Data<>(date, response.byteReceiveRate));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
