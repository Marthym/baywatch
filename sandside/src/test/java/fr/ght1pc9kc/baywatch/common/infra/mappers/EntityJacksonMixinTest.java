package fr.ght1pc9kc.baywatch.common.infra.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.config.jackson.mixins.EntityJacksonMixin;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.config.UserMixin;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

class EntityJacksonMixinTest {

    private final ObjectMapper tested = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .addMixIn(User.class, UserMixin.class)
            .addMixIn(Entity.class, EntityJacksonMixin.class);

    @Test
    void should_serialize_list() throws JsonProcessingException, JSONException {
        String actual = tested.writer().withDefaultPrettyPrinter()
                .writeValueAsString(List.of(UserSamples.OBIWAN, UserSamples.LUKE, UserSamples.YODA));

        JSONAssert.assertEquals("""
                [ {
                  "_id" : "3ebbc7f5326d7076b858d44e7bf6a5dac4e9adea0400ec778a7a51a817032bb2",
                  "_createdAt" : 0.0,
                  "login" : "okenobi",
                  "name" : "Obiwan Kenobi",
                  "mail" : "obiwan.kenobi@jedi.com",
                  "roles" : [ "MANAGER" ]
                }, {
                  "_id" : "b816278d7776e1449c537592f2f8911bc8760f78b3980db24d41dc7e9a551453",
                  "_createdAt" : 0.0,
                  "login" : "lskywalker",
                  "name" : "Luke Skywalker",
                  "mail" : "luke.skywalker@jedi.com",
                  "roles" : [ "USER", "MANAGER:TM01GP696RFPTY32WD79CVB0KDTF" ]
                }, {
                  "_id" : "3eff78846d4ae8d9987c975e9529775ceac8d92c840f3be2e0a89e1ecfd212c7",
                  "_createdAt" : 0.0,
                  "login" : "yoda",
                  "name" : "Yoda Master",
                  "mail" : "yoda@jedi.com",
                  "roles" : [ "ADMIN" ]
                } ]
                """, actual, true);
    }
}