package fr.ght1pc9kc.baywatch.common.infra.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.config.jackson.mixins.EntityJacksonMixin;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.config.PermissionMixin;
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
            .addMixIn(Permission.class, PermissionMixin.class)
            .addMixIn(Entity.class, EntityJacksonMixin.class);

    @Test
    void should_serialize_list() throws JsonProcessingException, JSONException {
        String actual = tested.writer().withDefaultPrettyPrinter()
                .writeValueAsString(List.of(UserSamples.OBIWAN, UserSamples.LUKE, UserSamples.YODA));

        JSONAssert.assertEquals("""
                [ {
                  "_id" : "US01GRQ11XKGHERDEBSCHBNJAY78",
                  "_createdAt" : 0.0,
                  "login" : "okenobi",
                  "name" : "Obiwan Kenobi",
                  "mail" : "obiwan.kenobi@jedi.com",
                  "roles" : [ "MANAGER" ]
                }, {
                  "_id" : "US01GRQ15DCEX52JH4GWJ26G33ME",
                  "_createdAt" : 0.0,
                  "login" : "lskywalker",
                  "name" : "Luke Skywalker",
                  "mail" : "luke.skywalker@jedi.com",
                  "roles" : [ "USER", "MANAGER:TM01GP696RFPTY32WD79CVB0KDTF" ]
                }, {
                  "_id" : "US01GRQ11X1W8E6NQER7E1FNQ7HC",
                  "_createdAt" : 0.0,
                  "login" : "yoda",
                  "name" : "Yoda Master",
                  "mail" : "yoda@jedi.com",
                  "roles" : [ "ADMIN" ]
                } ]
                """, actual, true);
    }
}