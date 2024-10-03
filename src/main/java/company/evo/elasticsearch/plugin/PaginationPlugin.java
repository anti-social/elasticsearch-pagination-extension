/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package company.evo.elasticsearch.plugin;

import company.evo.elasticsearch.PaginationExtBuilder;
import company.evo.elasticsearch.PaginationFilter;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.Collections;
import java.util.List;

public class PaginationPlugin extends Plugin
    implements ActionPlugin, SearchPlugin, ScriptPlugin
{
    private final Settings settings;

    public PaginationPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<ActionFilter> getActionFilters() {
        return Collections.singletonList(
            new PaginationFilter(settings)
        );
    }

    @Override
    public List<SearchExtSpec<?>> getSearchExts() {
        return Collections.singletonList(
            new SearchExtSpec<>(
                PaginationExtBuilder.NAME,
                PaginationExtBuilder::new,
                PaginationExtBuilder::fromXContent
            )
        );
    }
}
