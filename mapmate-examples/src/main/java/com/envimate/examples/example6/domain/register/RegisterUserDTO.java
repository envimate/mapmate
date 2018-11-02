/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.examples.example6.domain.register;

import com.envimate.examples.example6.DTO;
import com.envimate.examples.example6.domain.user.Password;
import com.envimate.examples.example6.domain.user.UserEmail;
import com.envimate.examples.example6.domain.user.UserName;

@DTO
public final class RegisterUserDTO {

    public final UserName userName;
    public final UserEmail userEmail;
    public final Password password;
    public final Address address;

    private RegisterUserDTO(UserName userName, UserEmail userEmail, Password password, Address address) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.address = address;
    }

    public static final RegisterUserDTO userAuth(
            final UserName userName,
            final UserEmail userEmail,
            final Password password,
            final Address address
    ) {
        return new RegisterUserDTO(userName, userEmail, password, address);
    }

}
