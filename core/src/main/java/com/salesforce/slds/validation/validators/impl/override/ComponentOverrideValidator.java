/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.validation.validators.impl.override;


import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.slds.shared.models.context.Context;
import com.salesforce.slds.shared.models.context.ContextKey;
import com.salesforce.slds.shared.models.core.Entry;
import com.salesforce.slds.shared.models.core.Input;
import com.salesforce.slds.shared.models.locations.Location;
import com.salesforce.slds.shared.models.locations.Range;
import com.salesforce.slds.shared.models.override.ComponentOverride;
import com.salesforce.slds.shared.models.recommendation.Action;
import com.salesforce.slds.shared.models.recommendation.ActionType;
import com.salesforce.slds.shared.models.recommendation.Item;
import com.salesforce.slds.shared.models.recommendation.RelatedInformation;
import com.salesforce.slds.validation.validators.SLDSValidator;
import com.salesforce.slds.validation.validators.interfaces.OverrideValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class ComponentOverrideValidator extends SLDSValidator implements OverrideValidator, InitializingBean {

    @Override
    public List<ComponentOverride> getOverrides(Entry entry, Context context){
        List<ComponentOverride> componentOverrides = Lists.newArrayList();

        if (context.isEnabled(ContextKey.OVERRIDE)) {
            entry.getInputs().stream()
                    .map(Input::asRuleSet)
                    .filter(Objects::nonNull)
                    .forEach(input -> {
                        Rule rule = input.getRule();

                        rule.selectors().stream().forEach(selector -> {
                            String selectorString = selector.toString(false);
                            if (componentOverrideSLDSMatch(selectorString)) {

                                String sldsComponentClass = getSldsClassAsString(selectorString);

                                ComponentOverride co = ComponentOverride
                                        .builder()
                                        .sldsComponentClass(sldsComponentClass)
                                        .selector(selector).build();

                                Action action = Action.builder()
                                        .name(sldsComponentClass)
                                        .value(sldsComponentClass)
                                        .actionType(ActionType.NONE)
                                        .range(co.getRange()).build();

                                componentOverrides.add(ComponentOverride
                                        .builder()
                                        .sldsComponentClass(sldsComponentClass)
                                        .selector(selector)
                                        .rule(input)
                                        .action(action).build());
                            }
                        });
                    });
        }

        return componentOverrides;

    }

    public String getSldsClassAsString(String selector){
        String[] selectorsArr = selector.split(" ");
        String lastSelector = selectorsArr[selectorsArr.length-1];
        String[] lastSelectorArr = lastSelector.split("[.#]");

        for (String potentialMatch : lastSelectorArr) {
            if (potentialMatch.startsWith("slds")){
                return potentialMatch;
            }
        }

        return null;
    }

    // Find occurence of .slds in selector any location
    Boolean componentOverrideSoftMatch(String selector){
        String[] selectorsArr = selector.split(" ");
        if (selectorsArr[selectorsArr.length-1].contains(".slds")){
            return true;
        }

        return false;
    }

    public Boolean componentOverrideSLDSMatch(String selector){
        return componentOverrideSoftMatch(selector) && VALID_UTILITY_CLASSES.contains(getSldsClassAsString(selector));
    }
}