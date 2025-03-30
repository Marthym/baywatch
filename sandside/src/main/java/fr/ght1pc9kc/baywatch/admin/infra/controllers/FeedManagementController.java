package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ADMIN')")
public class FeedManagementController {
}
