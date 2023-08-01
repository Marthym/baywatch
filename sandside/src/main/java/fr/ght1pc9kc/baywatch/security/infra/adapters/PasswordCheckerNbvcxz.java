package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.model.PasswordStrength;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.ConfigurationBuilder;
import me.gosimple.nbvcxz.resources.Dictionary;
import me.gosimple.nbvcxz.resources.DictionaryBuilder;
import me.gosimple.nbvcxz.scoring.Result;
import me.gosimple.nbvcxz.scoring.TimeEstimate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public record PasswordCheckerNbvcxz() implements PasswordStrengthChecker {
    @Override
    public PasswordStrength estimate(String password, User user, Locale locale) {
        List<Dictionary> dictionaryList = ConfigurationBuilder.getDefaultDictionaries();
        dictionaryList.add(new DictionaryBuilder()
                .setDictionaryName("exclude")
                .setExclusion(true)
                .addWord(user.login, 0)
                .addWord(user.name, 0)
                .addWord(user.mail, 0)
                .createDictionary());

        Configuration configuration = new ConfigurationBuilder()
                .setDictionaries(dictionaryList)
                .createConfiguration();

        Nbvcxz nbvcxz = new Nbvcxz(configuration);
        Result result = nbvcxz.estimate(password);
        return new PasswordStrength(
                result.isMinimumEntropyMet(),
                result.getEntropy(),
                TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_10")
        );
    }
}
