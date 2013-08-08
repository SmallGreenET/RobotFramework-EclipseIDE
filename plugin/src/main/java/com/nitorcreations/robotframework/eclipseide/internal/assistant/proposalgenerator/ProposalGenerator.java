/**
 * Copyright 2012-2013 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.internal.assistant.proposalgenerator;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class ProposalGenerator implements IProposalGenerator {

    @Override
    public void addTableProposals(IFile file, ParsedString argument, int documentOffset, Deque<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue(), proposalSets, new TableAttemptVisitor());
    }

    @Override
    public void addSettingTableProposals(IFile file, ParsedString argument, int documentOffset, Deque<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new SettingTableAttemptVisitor());
    }

    @Override
    public void addKeywordDefinitionProposals(final IFile file, ParsedString argument, int documentOffset, Deque<RobotCompletionProposalSet> proposalSets) {
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new KeywordDefinitionAttemptVisitor(file, argument));
    }

    @Override
    public void addKeywordCallProposals(IFile file, ParsedString argument, int documentOffset, Deque<RobotCompletionProposalSet> proposalSets) {
        // toLowerCase?
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new KeywordCallAttemptVisitor(file));
    }

    @Override
    public void addVariableProposals(IFile file, ParsedString argument, int documentOffset, Deque<RobotCompletionProposalSet> proposalSets, int maxVariableCharPos, int maxSettingCharPos) {
        // toLowerCase?
        acceptAttempts(argument, documentOffset, argument.getValue().toLowerCase(), proposalSets, new VariableAttemptVisitor(file, maxVariableCharPos, maxSettingCharPos));
    }

    private void acceptAttempts(ParsedString argument, int documentOffset, String lookFor, Deque<RobotCompletionProposalSet> proposalSets, AttemptVisitor attemptVisitor) {
        IRegion replacementRegion = new Region(argument.getArgCharPos(), argument.getValue().length());
        List<String> attempts = generateAttempts(argument, documentOffset, lookFor);
        for (String attempt : attempts) {
            RobotCompletionProposalSet proposalSet = attemptVisitor.visitAttempt(attempt, replacementRegion);
            if (proposalsContainsOnly(proposalSet.getProposals(), argument)) {
                // Found a single exact hit - probably means it was content-assisted earlier and the user now wants to
                // change it to something else
                continue;
            }
            if (!proposalSet.getProposals().isEmpty()) {
                proposalSet.setBasedOnInput(!attempt.isEmpty());
                proposalSets.add(proposalSet);
                return;
            }
        }
    }

    private boolean proposalsContainsOnly(List<RobotCompletionProposal> proposals, ParsedString argument) {
        return proposals.size() == 1 && proposals.get(0).getMatchArgument().getValue().equals(argument.getValue());
    }

    private List<String> generateAttempts(ParsedString argument, int documentOffset, String lookFor) {
        List<String> attempts = new ArrayList<String>(3);
        attempts.add(lookFor);
        int argumentOffset = documentOffset - argument.getArgCharPos();
        if (lookFor.length() > argumentOffset) {
            attempts.add(lookFor.substring(0, argumentOffset));
        }
        attempts.add("");
        return attempts;
    }
}
