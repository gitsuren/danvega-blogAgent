```mermaid
flowchart TD
  Start([Start])
  UserInput[/User Input/]
  Research["Research Topic\n(web search)\nOutput: ResearchedTopic"]
  WriteDraft["Write Draft\n(LLM: Personas.WRITER, JSON_OUTPUT)\nOutput: DraftPost"]
  Review["Review Draft\n(LLM: reviewer, JSON_OUTPUT)\nOutput: ReviewedPost"]
  ReviewCheck{"Review parse & quality OK?"}
  TLDR["Add TLDR\n(LLM default)\nOutput: FinalPost"]
  FrontMatter["Add Front Matter\n(LLM + readingStatsTool)\nOutput: FrontMatter + PublishedPost"]
  WriteFile["Write to file\n(persist .md)"]
  End([Goal: Reviewed & polished blog post with front matter])

  Start --> UserInput --> Research --> WriteDraft --> Review --> ReviewCheck
  ReviewCheck -- Yes --> TLDR
  ReviewCheck -- No / Retry --> Review
  TLDR --> FrontMatter --> WriteFile --> End

  %% Tools and integrations
  subgraph Tools
    direction TB
    WebTools[CoreToolGroups.WEB]
    ReadingStats[readingStatsTool]
  end

  Research --> WebTools
  FrontMatter --> ReadingStats

  %% LLM error/parse fallback (compact)
  ReviewCheck -- JSON parse error --> ParseFallback["Extract top-level JSON\n(JsonUtils.extractTopLevelJson)\nRetry parse"]
  ParseFallback --> ReviewCheck

  classDef llm fill:#eef,stroke:#333;
  class WriteDraft,Review,TLDR,FrontMatter llm;
