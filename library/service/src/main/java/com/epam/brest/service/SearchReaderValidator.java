package com.epam.brest.service;

import com.epam.brest.model.sample.SearchReaderSample;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.time.LocalDate;

@Service()
public class SearchReaderValidator implements ISearchReaderValidator {
    @Override
    public boolean supports(Class<?> aClass) {
        return SearchReaderSample.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SearchReaderSample srs = (SearchReaderSample) o;
        if(srs.getFrom() == null){
            srs.setFrom(LocalDate.of(1970, 01, 01));
        }
        if(srs.getTo() == null){
            srs.setTo(LocalDate.now());
        }
        if(srs.getFrom().isAfter(srs.getTo())){
           errors.rejectValue("from", "date.from.to", "date was not selected correctly");
        }
    }
}
