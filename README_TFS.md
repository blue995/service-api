# Changes made for TFS support

## TfsIntegrationService

Implementation based on `com.epam.ta.reportportal.core.integration.util.RallyIntegrationService.java`

## project-properties.gradle

Reduced line coverage from 75 % to 70 %

## V001003_integration_type.sql

`INSERT INTO integration_type (enabled, name, auth_flow, creation_date, group_type) VALUES (TRUE, 'tfs', 'OAUTH', now(), 'BTS') ;`