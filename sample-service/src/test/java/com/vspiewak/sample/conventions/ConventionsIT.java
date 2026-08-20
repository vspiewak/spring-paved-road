package com.vspiewak.sample.conventions;

import com.vspiewak.pavedroad.conventions.PlatformConventionsIT;
import com.vspiewak.sample.Containers;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

/** The platform runtime conventions, opted into — the subclass only wires the context. */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(Containers.class)
class ConventionsIT extends PlatformConventionsIT {}
