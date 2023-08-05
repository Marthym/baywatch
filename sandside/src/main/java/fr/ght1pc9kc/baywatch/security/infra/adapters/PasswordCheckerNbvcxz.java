package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.resources.Configuration;
import me.gosimple.nbvcxz.resources.ConfigurationBuilder;
import me.gosimple.nbvcxz.resources.Dictionary;
import me.gosimple.nbvcxz.resources.DictionaryBuilder;
import me.gosimple.nbvcxz.scoring.Result;
import me.gosimple.nbvcxz.scoring.TimeEstimate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Service
public record PasswordCheckerNbvcxz() implements PasswordStrengthChecker {

    @Override
    public PasswordEvaluation estimate(String password, Locale locale, Collection<String> exclude) {
        if (password == null || password.isEmpty()) {
            ResourceBundle mainResource = ResourceBundle.getBundle("main", locale, Configuration.class.getClassLoader());
            return new PasswordEvaluation(false, 0, mainResource.getString("main.estimate.instant"));
        }

        List<Dictionary> dictionaryList = ConfigurationBuilder.getDefaultDictionaries();
        dictionaryList.add(new DictionaryBuilder()
                .setDictionaryName("exclude")
                .setExclusion(true)
                .addWords(exclude, 0)
                .createDictionary());

        Configuration configuration = new ConfigurationBuilder()
                .setDictionaries(dictionaryList)
                .setLocale(Locale.forLanguageTag(locale.getLanguage()))
                .setMinimumEntropy(40d)
                .createConfiguration();

        Nbvcxz nbvcxz = new Nbvcxz(configuration);
        Result result = nbvcxz.estimate(password);
        return new PasswordEvaluation(
                result.isMinimumEntropyMet(),
                result.getEntropy(),
                TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_10")
        );
    }
}
