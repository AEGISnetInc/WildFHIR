/*
 * #%L
 * WildFHIR - wildfhir-model
 * %%
 * Copyright (C) 2024 AEGIS.net, Inc.
 * All rights reserved.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of AEGIS nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.aegis.fhir.service;

import java.util.logging.Logger;

/**
 * Singleton class to provide concurrency control to batch and transaction processing.
 * Initial functionality is to simply track the number of concurrent requests that are
 * currently being processed.
 * 
 * @author richard.ettema
 *
 */
public class BatchTransactionConcurrencyService {

	private static Logger log = Logger.getLogger("BatchTransactionConcurrencyService");

    private static BatchTransactionConcurrencyService me;

    private Integer concurrentNumber = 0;

    public static BatchTransactionConcurrencyService instance() {
		if (me == null) {
			synchronized (BatchTransactionConcurrencyService.class) {
				if (me == null) {
					me = new BatchTransactionConcurrencyService();
				}
			}
		}
		return me;
	}

    /**
     * The current concurrent number value can never be less than zero
     * 
     * @return Integer, new concurrent number value
     */
    public Integer decrement() {
    	concurrentNumber--;

    	if (concurrentNumber < 0) {
    		concurrentNumber = 0;
    	}

    	log.info("BatchTransactionConcurrencyService.decrement() - concurrent number is now " + concurrentNumber);

    	return concurrentNumber;
    }

    /**
     * CAUTION - This method does not restrict the maximum value for the
     * current concurrent number. The calling logic must manage this explicitly.
     * 
     * @return Integer, new concurrent number value
     */
    public Integer increment() {
    	concurrentNumber++;

    	log.info("BatchTransactionConcurrencyService.increment() - concurrent number is now " + concurrentNumber);

    	return concurrentNumber;
    }

    /**
     * If limit is zero, always return false; i.e. no limit
     * Else, check current concurrent number against limit
     * 
     * @param limit
     * @return boolean
     */
    public boolean isLimitExceeded(Integer limit) {
    	boolean isLimitExceeded = false;

    	if (limit > 0 && concurrentNumber >= limit) {
    		isLimitExceeded = true;
    	}

    	return isLimitExceeded;
    }

}
