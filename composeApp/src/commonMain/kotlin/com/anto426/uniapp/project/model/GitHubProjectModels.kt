package com.anto426.uniapp.project.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAuthor(
    val login: String,
    val name: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val company: String? = null,
    val blog: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("html_url") val url: String,
    val followers: Int = 0,
    val following: Int = 0,
    @SerialName("public_repos") val publicRepositories: Int = 0,
    @SerialName("public_gists") val publicGists: Int = 0,
    @SerialName("created_at") val joinedAt: String? = null,
)

@Serializable
data class GitHubLicense(val name: String, @SerialName("spdx_id") val identifier: String? = null)

@Serializable
data class GitHubRepositoryInfo(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("html_url") val url: String,
    val description: String? = null,
    val language: String? = null,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("forks_count") val forks: Int = 0,
    @SerialName("open_issues_count") val openIssues: Int = 0,
    @SerialName("default_branch") val defaultBranch: String? = null,
    val license: GitHubLicense? = null,
    val archived: Boolean = false,
    val fork: Boolean = false,
)

@Serializable
data class GitHubContributor(
    val id: Long? = null,
    val login: String? = null,
    val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("html_url") val url: String? = null,
    val type: String? = null,
    val contributions: Int = 0,
)

@Serializable
data class GitHubProjectSnapshot(
    val author: GitHubAuthor? = null,
    val project: GitHubRepositoryInfo? = null,
    val repositories: List<GitHubRepositoryInfo> = emptyList(),
    val contributors: List<GitHubContributor> = emptyList(),
    val fetchedAt: Long = 0,
    val incomplete: Boolean = false,
)
