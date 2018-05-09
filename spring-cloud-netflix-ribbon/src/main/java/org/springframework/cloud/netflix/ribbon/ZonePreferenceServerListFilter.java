/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.netflix.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext.ContextKey;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAffinityServerListFilter;

/**
 * A filter that actively prefers the local zone (as defined by the deployment context, or
 * the Eureka instance metadata).
 *
 * @author Dave Syer
 */
public class ZonePreferenceServerListFilter extends ZoneAffinityServerListFilter<Server> {

	private String zone;

	@Override
	public void initWithNiwsConfig(IClientConfig niwsClientConfig) {
		super.initWithNiwsConfig(niwsClientConfig);
		if (ConfigurationManager.getDeploymentContext() != null) {
			this.zone = ConfigurationManager.getDeploymentContext().getValue(
					ContextKey.zone);
		}
	}

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		//获得“区域感知”的服务实例列表
		List<Server> output = super.getFilteredListOfServers(servers);
		if (this.zone != null && output.size() == servers.size()) {
			List<Server> local = new ArrayList<>();
			for (Server server : output) {
				// 比较
				if (this.zone.equalsIgnoreCase(server.getZone())) {
					// 增加
					local.add(server);
				}
			}
			// 是否为 空
			if (!local.isEmpty()) {
				// 不为空 直接返回
				return local;
			}
		}
		// 返回父类结果
		return output;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ZonePreferenceServerListFilter that = (ZonePreferenceServerListFilter) o;
		return Objects.equals(zone, that.zone);
	}

	@Override
	public int hashCode() {
		return Objects.hash(zone);
	}

	@Override
	public String toString() {
		return new StringBuilder("ZonePreferenceServerListFilter{")
				.append("zone='").append(zone).append("'")
				.append("}").toString();
	}

}
