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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public record PasswordCheckerNbvcxz() implements PasswordStrengthChecker {

    @Override
    public PasswordEvaluation estimate(String password, Locale locale, Collection<String> exclude) {
        ResourceBundle mainResource = ResourceBundle.getBundle("main", locale, Configuration.class.getClassLoader());
        if (password == null || password.isEmpty()) {
            return new PasswordEvaluation(false, 0, mainResource.getString("main.estimate.instant"));
        }

        Set<String> excludeWords = exclude.stream()
                .flatMap(word -> Arrays.stream(word.split("[ @_-]")))
                .collect(Collectors.toUnmodifiableSet());
        List<Dictionary> dictionaryList = ConfigurationBuilder.getDefaultDictionaries();
        dictionaryList.add(new DictionaryBuilder()
                .setDictionaryName("exclude")
                .setExclusion(true)
                .addWords(excludeWords, 0)
                .createDictionary());

        Configuration configuration = new ConfigurationBuilder()
                .setDictionaries(dictionaryList)
                .setLocale(Locale.forLanguageTag(locale.getLanguage()))
                .setMinimumEntropy(40d)
                .createConfiguration();

        Nbvcxz nbvcxz = new Nbvcxz(configuration);
        Result result = nbvcxz.estimate(password);

        String message = Optional.ofNullable(result.getFeedback())
                .flatMap(fb -> Optional.ofNullable(fb.getWarning()))
                .map(w -> w + " ")
                .orElse("") +
                mainResource.getString("main.timeToCrack") + " " +
                TimeEstimate.getTimeToCrackFormatted(result, "OFFLINE_BCRYPT_10");
        return new PasswordEvaluation(
                result.isMinimumEntropyMet(),
                result.getEntropy(),
                message
        );
    }
}
