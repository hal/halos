/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.halos.client.config;

import java.util.Set;

import static org.wildfly.halos.client.config.Role.ADMINISTRATOR;
import static org.wildfly.halos.client.config.Role.SUPER_USER;

/** Holds information about an user. */
public class User {

    public final Set<Role> roles;
    public String name;

    public User(String name, Set<Role> roles) {
        this.name = name;
        this.roles = roles;
    }

    public void refreshRoles(Set<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    /** @return true if this user belongs to the role SuperUser, false otherwise. */
    public boolean isSuperuser() {
        for (Role role : roles) {
            if (SUPER_USER.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /** @return true if this user belongs to the role Administrator, false otherwise. */
    public boolean isAdministrator() {
        for (Role role : roles) {
            if (ADMINISTRATOR.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
