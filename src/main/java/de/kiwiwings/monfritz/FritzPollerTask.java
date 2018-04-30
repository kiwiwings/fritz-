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

import javafx.concurrent.Task;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Poller which requests the data from the Fritz.Box
 * inspired by http://dede67.bplaced.net/PhythonScripte/bwm/Bandbreitenmonitor.html
 *
 * @param <T> the response class
 */
public class FritzPollerTask<T> extends Task<T> {
    private static final String FritzURL = "http://192.168.178.1:49000/igdupnp/control/WANCommonIFC1";


    private static final String requestTmpl =
        "<?xml version=\"1.0\"?>\n" +
        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
        "        s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
        "  <s:Body>\n" +
        "    <u:%s xmlns:u=\"urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1\"/>\n" +
        "  </s:Body>\n" +
        "</s:Envelope>";

    private final String ifcAction;
    private final Class<T> responseClass;
    private final XMLInputFactory xif;
    private final Unmarshaller unmarshaller;

    static FritzPollerTask<GetAddonInfosResponse> getAddonInfos() {
        return new FritzPollerTask<>(GetAddonInfosResponse.class, "GetAddonInfos");
    }

    static FritzPollerTask<GetCommonLinkPropertiesResponse> getCommonLinkProperties() {
        return new FritzPollerTask<>(GetCommonLinkPropertiesResponse.class, "GetCommonLinkProperties");
    }


    private FritzPollerTask(Class<T> responseClass, String ifcAction) {
        this.ifcAction = ifcAction;
        this.responseClass = responseClass;

        xif = XMLInputFactory.newInstance();

        try {
            final JAXBContext jc = JAXBContext.newInstance(responseClass);
            unmarshaller = jc.createUnmarshaller();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected T call() {
        running();

        try {
            final byte[] request = String.format(requestTmpl, ifcAction).getBytes("UTF-8");

            final URL url = new URL(FritzURL);

            final byte[] buf = new byte[1024];

            final HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", "text/xml; charset=\"utf-8\"");
            con.setRequestProperty("SOAPACTION", "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#" + ifcAction);
            con.setDoInput(true);
            con.setDoOutput(true);

            try (final OutputStream os = con.getOutputStream()) {
                os.write(request);
            }

            JAXBElement<T> je;
            try (InputStream is = con.getInputStream()) {
                XMLStreamReader xsr = xif.createXMLStreamReader(is);
                xsr.nextTag(); // Advance to Envelope tag
                xsr.nextTag(); // Advance to Body tag
                xsr.nextTag(); // Advance to getNumberResponse tag

                je = unmarshaller.unmarshal(xsr, responseClass);

                //noinspection StatementWithEmptyBody
                while (is.read(buf) != -1) {
                }
            }

            // to allow keep-alive on HttpUrlConnection, the InputStreams needs to be read completely
            // and the connection disconnected (https://stackoverflow.com/questions/9943351)
            con.disconnect();

            updateValue(je.getValue());

            succeeded();

            return je.getValue();
        } catch (Exception ex) {
            ex.printStackTrace();
            succeeded();
            try {
                return responseClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
