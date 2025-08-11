package fr.ght1pc9kc.baywatch.admin.infra.samples;

import fr.ght1pc9kc.baywatch.dsl.tables.records.ConfigurationRecord;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.List;

public class ConfigurationRecordSamples implements RelationalDataSet<ConfigurationRecord> {
    public static final ConfigurationRecordSamples SAMPLE = new ConfigurationRecordSamples();

    @Override
    public List<ConfigurationRecord> records() {
        return List.of(
                new ConfigurationRecord("CF01K2F88DYNCAPEAKCZEWXY3Z7N", "jedi.master", "Yoda"),
                new ConfigurationRecord("CF01K2F88EB6VJMH79EGGTTED040", "jedi.knight", "Luke Skywalker"),

                new ConfigurationRecord("CF01K2F88ERJ2FZ7EQSXVM8XVWT0", "sith.lord", "Darth Vader"),
                new ConfigurationRecord("CF01K2F88F4DMWW18XPDT7N8EJE5", "sith.apprentice", "Darth Sidious"),

                new ConfigurationRecord("CF01K2F88FG9WNNWED3P57WCE125", "lightsaber.color.jedi", "green,blue"),
                new ConfigurationRecord("CF01K2F88FX1CCQG1A0WVM1VJW6E", "lightsaber.color.sith", "red"),

                new ConfigurationRecord("CF01K2F88G9XYWS9PXC4Y4X0EQAR", "force.power.level", "9001")
        );
    }
}
