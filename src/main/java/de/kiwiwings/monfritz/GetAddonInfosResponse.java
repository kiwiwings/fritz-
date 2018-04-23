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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GetAddonInfosResponse", namespace = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1")
public class GetAddonInfosResponse {
    @XmlElement(name = "NewByteSendRate")
    public Integer byteSendRate;

    @XmlElement(name = "NewByteReceiveRate")
    public Integer byteReceiveRate;

    @XmlElement(name = "NewPacketSendRate")
    public Integer packetSendRate;

    @XmlElement(name = "NewPacketReceiveRate")
    public Integer packetReceiveRate;

    @XmlElement(name = "NewTotalBytesSent")
    public Long totalBytesSent;

    @XmlElement(name = "NewTotalBytesReceived")
    public Long totalBytesReceived;

    @XmlElement(name = "NewAutoDisconnectTime")
    public Integer autoDisconnectTime;

    @XmlElement(name = "NewIdleDisconnectTime")
    public Integer idleDisconnectTime;

    @XmlElement(name = "NewDNSServer1")
    public String dnsServer1;

    @XmlElement(name = "NewDNSServer2")
    public String dnsServer2;

    @XmlElement(name = "NewVoipDNSServer1")
    public String voipDNSServer1;

    @XmlElement(name = "NewVoipDNSServer2")
    public String voipDNSServer2;

    @XmlElement(name = "NewUpnpControlEnabled")
    public Boolean upnpControlEnabled;

    @XmlElement(name = "NewRoutedBridgedModeBoth")
    public Boolean routedBridgedModeBoth;
}
