package com.clubportal.config;

import com.clubportal.model.Club;
import com.clubportal.model.ClubAdmin;
import com.clubportal.model.ClubCommunityAnswer;
import com.clubportal.model.ClubCommunityQuestion;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.TimeSlot;
import com.clubportal.model.User;
import com.clubportal.model.Venue;
import com.clubportal.repository.BookingHoldRepository;
import com.clubportal.repository.BookingRecordRepository;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ChatSessionRepository;
import com.clubportal.repository.CheckoutSessionRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubChatKbEntryRepository;
import com.clubportal.repository.ClubCommunityAnswerRepository;
import com.clubportal.repository.ClubCommunityQuestionRepository;
import com.clubportal.repository.ClubImageRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.PasswordResetTokenRepository;
import com.clubportal.repository.ProfileEmailChangeVerificationRepository;
import com.clubportal.repository.RegistrationEmailVerificationRepository;
import com.clubportal.repository.TimeSlotRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.repository.VenueRepository;
import com.clubportal.util.ClubTagCodec;
import com.clubportal.util.PasswordEncryptionUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class ClubDatasetSeeder {

    private static final ZoneId LONDON = ZoneId.of("Europe/London");
    private static final String COMMUNITY_MEMBER_PASSWORD = "SeedMember123";
    private static final List<CommunityMemberSeed> COMMUNITY_MEMBER_SEEDS = List.of(
            member("Maya Turner", "seed-member-01@example.test"),
            member("Ethan Cole", "seed-member-02@example.test"),
            member("Sofia Bennett", "seed-member-03@example.test"),
            member("Noah Patel", "seed-member-04@example.test")
    );

    private static final List<ClubSeed> CLUB_SEEDS = List.of(
            club(
                    "Apex Shuttle & Pickleball Club",
                    "Badminton-focused training nights with weekly ladder games and a shared pickleball social on Saturdays.",
                    "Stratford Sports Quarter",
                    List.of("Badminton", "Pickleball"),
                    "07:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Shuttle Hall A", 16),
                            venue("Shuttle Hall B", 16)
                    ),
                    List.of(slot("18:00", 90), slot("20:00", 90)),
                    14.00,
                    49.00
            ),
            club(
                    "Kings Cross Hoops Society",
                    "Full-court basketball training with evening scrimmages and coach-led shooting clinics for mixed levels.",
                    "Kings Cross Arena District",
                    List.of("Basketball"),
                    "08:00",
                    "22:30",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    List.of(
                            venue("Court 1", 20),
                            venue("Court 2", 20)
                    ),
                    List.of(slot("18:30", 90), slot("20:15", 90)),
                    18.00,
                    59.00
            ),
            club(
                    "Riverside Football Union",
                    "Five-a-side and seven-a-side football bookings with weekly squad blocks and Sunday technical sessions.",
                    "Wandsworth Riverside Fields",
                    List.of("Football"),
                    "08:00",
                    "22:30",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
                    List.of(
                            venue("Pitch East", 24),
                            venue("Pitch West", 24)
                    ),
                    List.of(slot("19:00", 90), slot("20:45", 90)),
                    21.00,
                    65.00
            ),
            club(
                    "Baseline Tennis & Padel House",
                    "Competitive tennis programming with selected padel crossover sessions and bookable evening courts.",
                    "Chelsea Harbour Courts",
                    List.of("Tennis", "Padel"),
                    "07:00",
                    "21:30",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Clay Court 1", 8),
                            venue("Hard Court 2", 8)
                    ),
                    List.of(slot("17:30", 90), slot("19:15", 90)),
                    22.00,
                    72.00
            ),
            club(
                    "Docklands Spikers Club",
                    "Indoor volleyball bookings for rec and intermediate players, with team blocks and open-play weekends.",
                    "Canary Wharf Activity Hub",
                    List.of("Volleyball"),
                    "09:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
                    List.of(
                            venue("Indoor Court Alpha", 18),
                            venue("Indoor Court Beta", 18)
                    ),
                    List.of(slot("18:00", 90), slot("19:45", 90)),
                    15.00,
                    46.00
            ),
            club(
                    "Blue Mile Aquatics",
                    "Lane-book swimming sessions for early birds and after-work training groups with coach-supervised blocks.",
                    "Olympic Park Aquatic Centre",
                    List.of("Swimming"),
                    "06:00",
                    "21:00",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Lane Block A", 8),
                            venue("Lane Block B", 8)
                    ),
                    List.of(slot("06:30", 60), slot("07:45", 60)),
                    12.00,
                    44.00
            ),
            club(
                    "City Stride Running Crew",
                    "Structured running meetups for tempo work, river loops, and social long-run blocks before weekend races.",
                    "South Bank Run Pavilion",
                    List.of("Running"),
                    "06:00",
                    "21:30",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("River Loop Start", 30),
                            venue("Bridge Interval Track", 24)
                    ),
                    List.of(slot("06:45", 75), slot("18:30", 75)),
                    10.00,
                    35.00
            ),
            club(
                    "Ember Cycle Studio",
                    "High-energy cycling studio blocks with coached interval sessions and endurance rides after office hours.",
                    "Shoreditch Ride Lab",
                    List.of("Cycling"),
                    "06:30",
                    "21:30",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    List.of(
                            venue("Studio Bikes East", 18),
                            venue("Studio Bikes West", 18)
                    ),
                    List.of(slot("07:00", 60), slot("19:00", 60)),
                    13.00,
                    42.00
            ),
            club(
                    "Stillpoint Yoga & Mobility Club",
                    "Restorative yoga classes with mobility support sessions for members who want recovery-focused weekday blocks.",
                    "Hampstead Wellness Loft",
                    List.of("Yoga", "Mobility"),
                    "07:00",
                    "21:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Sun Room", 20),
                            venue("Moon Room", 16)
                    ),
                    List.of(slot("07:30", 60), slot("18:15", 60)),
                    11.00,
                    38.00
            ),
            club(
                    "SpinLine Table Tennis Lab",
                    "Fast-format table tennis practice with reserved table blocks, partner matching, and weekly challenge ladders.",
                    "Waterloo Games Hub",
                    List.of("Table Tennis"),
                    "09:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Table Grid A", 12),
                            venue("Table Grid B", 12)
                    ),
                    List.of(slot("18:00", 75), slot("19:30", 75)),
                    12.00,
                    39.00
            ),
            club(
                    "Iron Lantern Boxing Club",
                    "Pad, bag, and ring sessions with compact groups and bookable evening technical classes for boxing members.",
                    "Brixton Fight Factory",
                    List.of("Boxing"),
                    "08:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
                    List.of(
                            venue("Ring One", 14),
                            venue("Bag Floor", 20)
                    ),
                    List.of(slot("18:30", 75), slot("20:15", 75)),
                    16.00,
                    54.00
            ),
            club(
                    "Oval Wicket Society",
                    "Cricket net practice and bowling machine blocks with evening and weekend sessions for developing squads.",
                    "Oval Practice Centre",
                    List.of("Cricket"),
                    "08:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
                    List.of(
                            venue("Net Lane 1", 14),
                            venue("Net Lane 2", 14)
                    ),
                    List.of(slot("17:30", 90), slot("19:15", 90)),
                    17.00,
                    57.00
            ),
            club(
                    "North Line Rugby Collective",
                    "Rugby conditioning, contact technique, and tactical blocks with reserved training pitches three times a week.",
                    "Richmond Training Grounds",
                    List.of("Rugby"),
                    "08:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Pitch Red", 26),
                            venue("Pitch Blue", 26)
                    ),
                    List.of(slot("19:00", 90), slot("20:45", 90)),
                    20.00,
                    63.00
            ),
            club(
                    "Summit Rope & Yoga Club",
                    "Climbing sessions across bouldering and lead walls, paired with a recovery yoga block for members.",
                    "Bermondsey Vertical Yard",
                    List.of("Climbing", "Yoga"),
                    "09:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    List.of(
                            venue("Boulder Hall", 22),
                            venue("Lead Wall", 18)
                    ),
                    List.of(slot("17:45", 90), slot("19:30", 90)),
                    19.00,
                    61.00
            ),
            club(
                    "Silver Foil Fencing Room",
                    "Foil-focused fencing classes with open piste reservations and coached footwork sessions for all levels.",
                    "Holborn Salle Quarter",
                    List.of("Fencing"),
                    "09:00",
                    "21:30",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Piste 1", 14),
                            venue("Piste 2", 14)
                    ),
                    List.of(slot("18:00", 75), slot("19:30", 75)),
                    15.00,
                    48.00
            ),
            club(
                    "Harbour Oars & Run Club",
                    "Rowing erg and launch bookings with supporting conditioning runs for members training across two disciplines.",
                    "Putney Boathouse Row",
                    List.of("Rowing", "Running"),
                    "06:00",
                    "21:00",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Erg Studio", 16),
                            venue("Launch Dock", 12)
                    ),
                    List.of(slot("06:15", 75), slot("18:00", 75)),
                    18.00,
                    56.00
            ),
            club(
                    "Green Nine Golf Studio",
                    "Indoor golf simulator bays with coached short-game clinics and reserved swing analysis sessions.",
                    "Greenwich Swing Centre",
                    List.of("Golf"),
                    "09:00",
                    "21:30",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
                    List.of(
                            venue("Bay 1", 6),
                            venue("Bay 2", 6)
                    ),
                    List.of(slot("17:00", 60), slot("18:30", 60)),
                    23.00,
                    74.00
            ),
            club(
                    "Coreline Pilates Club",
                    "Pilates reformer and mat sessions for small groups, with bookable morning and evening movement blocks.",
                    "Marylebone Movement House",
                    List.of("Pilates"),
                    "07:00",
                    "21:00",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                    List.of(
                            venue("Reformer Studio", 10),
                            venue("Mat Studio", 16)
                    ),
                    List.of(slot("07:00", 60), slot("18:15", 60)),
                    14.00,
                    45.00
            ),
            club(
                    "Pulse Dance Theatre Club",
                    "Dance training for choreography, performance prep, and open studio rehearsals across contemporary styles.",
                    "Soho Performance Rooms",
                    List.of("Dance"),
                    "09:00",
                    "22:00",
                    EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Studio Red", 20),
                            venue("Studio Blue", 20)
                    ),
                    List.of(slot("18:15", 75), slot("20:00", 75)),
                    13.00,
                    41.00
            ),
            club(
                    "Sakura Judo Academy",
                    "Judo drilling and sparring reservations with structured dojo blocks for technique and randori practice.",
                    "White City Dojo Centre",
                    List.of("Judo"),
                    "08:00",
                    "21:30",
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                    List.of(
                            venue("Tatami Hall A", 18),
                            venue("Tatami Hall B", 18)
                    ),
                    List.of(slot("18:00", 75), slot("19:30", 75)),
                    15.00,
                    47.00
            )
    );

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubAdminRepository clubAdminRepository;
    private final ClubImageRepository clubImageRepository;
    private final VenueRepository venueRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final UserMembershipRepository userMembershipRepository;
    private final BookingRecordRepository bookingRecordRepository;
    private final BookingHoldRepository bookingHoldRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ClubChatKbEntryRepository clubChatKbEntryRepository;
    private final ClubCommunityQuestionRepository clubCommunityQuestionRepository;
    private final ClubCommunityAnswerRepository clubCommunityAnswerRepository;
    private final RegistrationEmailVerificationRepository registrationEmailVerificationRepository;
    private final ProfileEmailChangeVerificationRepository profileEmailChangeVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncryptionUtil passwordEncryptionUtil;

    public ClubDatasetSeeder(UserRepository userRepository,
                             ClubRepository clubRepository,
                             ClubAdminRepository clubAdminRepository,
                             ClubImageRepository clubImageRepository,
                             VenueRepository venueRepository,
                             TimeSlotRepository timeSlotRepository,
                             MembershipPlanRepository membershipPlanRepository,
                             UserMembershipRepository userMembershipRepository,
                             BookingRecordRepository bookingRecordRepository,
                             BookingHoldRepository bookingHoldRepository,
                             CheckoutSessionRepository checkoutSessionRepository,
                             TransactionRecordRepository transactionRecordRepository,
                             ChatSessionRepository chatSessionRepository,
                             ChatMessageRepository chatMessageRepository,
                             ClubChatKbEntryRepository clubChatKbEntryRepository,
                             ClubCommunityQuestionRepository clubCommunityQuestionRepository,
                             ClubCommunityAnswerRepository clubCommunityAnswerRepository,
                             RegistrationEmailVerificationRepository registrationEmailVerificationRepository,
                             ProfileEmailChangeVerificationRepository profileEmailChangeVerificationRepository,
                             PasswordResetTokenRepository passwordResetTokenRepository,
                             PasswordEncryptionUtil passwordEncryptionUtil) {
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.clubAdminRepository = clubAdminRepository;
        this.clubImageRepository = clubImageRepository;
        this.venueRepository = venueRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.userMembershipRepository = userMembershipRepository;
        this.bookingRecordRepository = bookingRecordRepository;
        this.bookingHoldRepository = bookingHoldRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.clubChatKbEntryRepository = clubChatKbEntryRepository;
        this.clubCommunityQuestionRepository = clubCommunityQuestionRepository;
        this.clubCommunityAnswerRepository = clubCommunityAnswerRepository;
        this.registrationEmailVerificationRepository = registrationEmailVerificationRepository;
        this.profileEmailChangeVerificationRepository = profileEmailChangeVerificationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncryptionUtil = passwordEncryptionUtil;
    }

    public int expectedClubCount() {
        return CLUB_SEEDS.size();
    }

    public List<String> expectedClubEmails() {
        return IntStream.rangeClosed(1, CLUB_SEEDS.size())
                .mapToObj(ordinal -> ordinal + "@" + ordinal + ".com")
                .toList();
    }

    public CurrentClubSnapshot snapshotCurrentClubDataset() {
        List<String> clubNames = clubRepository.findAll().stream()
                .map(Club::getClubName)
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .toList();
        List<String> clubEmails = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.CLUB)
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .sorted()
                .toList();
        return new CurrentClubSnapshot(clubNames, clubEmails);
    }

    public boolean isCanonicalSeedPresent() {
        Set<String> expectedEmails = new java.util.LinkedHashSet<>(expectedClubEmails());
        Set<String> clubEmails = clubRepository.findAll().stream()
                .map(Club::getEmail)
                .map(ClubDatasetSeeder::safe)
                .map(email -> email.toLowerCase(Locale.ROOT))
                .filter(email -> !email.isBlank())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> clubUserEmails = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.CLUB)
                .map(User::getEmail)
                .map(ClubDatasetSeeder::safe)
                .map(email -> email.toLowerCase(Locale.ROOT))
                .filter(email -> !email.isBlank())
                .collect(java.util.stream.Collectors.toSet());
        return clubEmails.containsAll(expectedEmails) && clubUserEmails.containsAll(expectedEmails);
    }

    @Transactional
    public SeedExecutionResult reseedForCurrentWindow() {
        LocalDate startDate = LocalDate.now(LONDON);
        LocalDate endDate = LocalDate.of(startDate.getYear(), Month.MAY, 17);
        if (endDate.isBefore(startDate)) {
            endDate = startDate.plusDays(42);
        }
        return reseed(startDate, endDate);
    }

    @Transactional
    public SeedExecutionResult reseed(LocalDate startDate, LocalDate endDate) {
        CurrentClubSnapshot previous = snapshotCurrentClubDataset();
        resetClubDataset();
        List<User> communityMembers = ensureCommunityMembers();

        for (int index = 0; index < CLUB_SEEDS.size(); index++) {
            seedClub(index + 1, CLUB_SEEDS.get(index), startDate, endDate, communityMembers);
        }

        return new SeedExecutionResult(
                previous.clubNames(),
                previous.clubEmails(),
                startDate,
                endDate,
                CLUB_SEEDS.size()
        );
    }

    @Transactional
    public CommunityQaSeedResult backfillCommunityQaIfMissing() {
        List<User> communityMembers = ensureCommunityMembers();
        Map<Integer, Integer> clubAdminUserByClubId = new HashMap<>();
        for (ClubAdmin clubAdmin : clubAdminRepository.findAll()) {
            if (clubAdmin.getClubId() != null && clubAdmin.getUserId() != null) {
                clubAdminUserByClubId.putIfAbsent(clubAdmin.getClubId(), clubAdmin.getUserId());
            }
        }

        List<Club> clubs = new ArrayList<>(clubRepository.findAll());
        clubs.sort(Comparator.comparing(Club::getClubId, Comparator.nullsLast(Integer::compareTo)));

        int clubsUpdated = 0;
        int questionsCreated = 0;
        int answersCreated = 0;

        for (int index = 0; index < clubs.size(); index++) {
            Club club = clubs.get(index);
            if (club == null || club.getClubId() == null) {
                continue;
            }
            if (!clubCommunityQuestionRepository.findByClubIdOrderByUpdatedAtDescQuestionIdDesc(club.getClubId()).isEmpty()) {
                continue;
            }
            CommunityQaSeedCounts counts = seedCommunityQa(
                    club,
                    clubAdminUserByClubId.get(club.getClubId()),
                    communityMembers,
                    index + 1
            );
            if (counts.questionsCreated() > 0 || counts.answersCreated() > 0) {
                clubsUpdated++;
                questionsCreated += counts.questionsCreated();
                answersCreated += counts.answersCreated();
            }
        }

        return new CommunityQaSeedResult(clubsUpdated, questionsCreated, answersCreated);
    }

    @Transactional
    public CanonicalContentRefreshResult refreshCanonicalOperationalData() {
        LocalDate startDate = LocalDate.now(LONDON);
        LocalDate endDate = LocalDate.of(startDate.getYear(), Month.MAY, 17);
        if (endDate.isBefore(startDate)) {
            endDate = startDate.plusDays(42);
        }
        return refreshCanonicalOperationalData(startDate, endDate);
    }

    @Transactional
    public CanonicalContentRefreshResult refreshCanonicalOperationalData(LocalDate startDate, LocalDate endDate) {
        if (!isCanonicalSeedPresent()) {
            throw new IllegalStateException("Canonical 20-club dataset is not present. Refusing in-place content refresh.");
        }

        List<CanonicalClubBinding> bindings = resolveCanonicalClubBindings();
        CanonicalRefreshGuard guard = inspectCanonicalRefreshGuard(bindings);
        if (guard.hasBlockingData()) {
            throw new IllegalStateException(
                    "Refusing canonical club content refresh because live data exists. "
                            + "memberships=" + guard.membershipsInUse()
                            + ", bookingRecords=" + guard.bookingRecords()
                            + ", bookingHolds=" + guard.bookingHolds()
            );
        }

        List<User> communityMembers = ensureCommunityMembers();
        int clubsUpdated = 0;
        int plansDeleted = 0;
        int plansCreated = 0;
        int timeslotsDeleted = 0;
        int timeslotsCreated = 0;
        int questionsDeleted = 0;
        int questionsCreated = 0;
        int answersDeleted = 0;
        int answersCreated = 0;

        for (CanonicalClubBinding binding : bindings) {
            ClubOperationalRefreshCounts counts = refreshCanonicalClubOperationalData(binding, startDate, endDate, communityMembers);
            clubsUpdated++;
            plansDeleted += counts.plansDeleted();
            plansCreated += counts.plansCreated();
            timeslotsDeleted += counts.timeslotsDeleted();
            timeslotsCreated += counts.timeslotsCreated();
            questionsDeleted += counts.questionsDeleted();
            questionsCreated += counts.questionsCreated();
            answersDeleted += counts.answersDeleted();
            answersCreated += counts.answersCreated();
        }

        return new CanonicalContentRefreshResult(
                clubsUpdated,
                plansDeleted,
                plansCreated,
                timeslotsDeleted,
                timeslotsCreated,
                questionsDeleted,
                questionsCreated,
                answersDeleted,
                answersCreated,
                startDate,
                endDate
        );
    }

    private List<CanonicalClubBinding> resolveCanonicalClubBindings() {
        Map<String, Club> clubByEmail = new HashMap<>();
        for (Club club : clubRepository.findAll()) {
            String email = safe(club == null ? null : club.getEmail()).toLowerCase(Locale.ROOT);
            if (!email.isBlank()) {
                clubByEmail.putIfAbsent(email, club);
            }
        }

        Map<Integer, Integer> clubAdminUserByClubId = new HashMap<>();
        for (ClubAdmin clubAdmin : clubAdminRepository.findAll()) {
            if (clubAdmin.getClubId() != null && clubAdmin.getUserId() != null) {
                clubAdminUserByClubId.putIfAbsent(clubAdmin.getClubId(), clubAdmin.getUserId());
            }
        }

        List<CanonicalClubBinding> bindings = new ArrayList<>();
        for (int ordinal = 1; ordinal <= CLUB_SEEDS.size(); ordinal++) {
            String email = (ordinal + "@" + ordinal + ".com").toLowerCase(Locale.ROOT);
            Club club = clubByEmail.get(email);
            if (club == null || club.getClubId() == null) {
                throw new IllegalStateException("Missing canonical club for email " + email);
            }
            bindings.add(new CanonicalClubBinding(
                    ordinal,
                    club,
                    CLUB_SEEDS.get(ordinal - 1),
                    clubAdminUserByClubId.get(club.getClubId())
            ));
        }
        return bindings;
    }

    private CanonicalRefreshGuard inspectCanonicalRefreshGuard(List<CanonicalClubBinding> bindings) {
        Set<Integer> clubIds = bindings.stream()
                .map(CanonicalClubBinding::club)
                .map(Club::getClubId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        List<MembershipPlan> existingPlans = membershipPlanRepository.findAll().stream()
                .filter(plan -> plan != null && plan.getClubId() != null && clubIds.contains(plan.getClubId()))
                .toList();
        List<Integer> planIds = existingPlans.stream()
                .map(MembershipPlan::getPlanId)
                .filter(id -> id != null)
                .toList();
        long membershipsInUse = planIds.isEmpty()
                ? 0
                : userMembershipRepository.findByPlanIdInOrderByCreatedAtDesc(planIds).size();

        List<Venue> existingVenues = venueRepository.findAll().stream()
                .filter(venue -> venue != null && venue.getClubId() != null && clubIds.contains(venue.getClubId()))
                .toList();
        Set<Integer> venueIds = existingVenues.stream()
                .map(Venue::getVenueId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        List<Integer> timeslotIds = venueIds.isEmpty()
                ? List.of()
                : timeSlotRepository.findAll().stream()
                .filter(slot -> slot != null && slot.getVenueId() != null && venueIds.contains(slot.getVenueId()))
                .map(TimeSlot::getTimeslotId)
                .filter(id -> id != null)
                .toList();

        long bookingRecords = timeslotIds.isEmpty()
                ? 0
                : bookingRecordRepository.findByTimeslotIdInOrderByBookingTimeAsc(timeslotIds).size();
        long bookingHolds = bookingHoldRepository.findAll().stream()
                .filter(hold -> hold != null
                        && hold.getClubId() != null
                        && clubIds.contains(hold.getClubId())
                        && !"RELEASED".equalsIgnoreCase(safe(hold.getStatus())))
                .count();

        return new CanonicalRefreshGuard(membershipsInUse, bookingRecords, bookingHolds);
    }

    private ClubOperationalRefreshCounts refreshCanonicalClubOperationalData(CanonicalClubBinding binding,
                                                                             LocalDate startDate,
                                                                             LocalDate endDate,
                                                                             List<User> communityMembers) {
        Club club = binding.club();
        ClubSeed seed = binding.seed();
        int ordinal = binding.ordinal();

        club.setClubName(seed.name());
        club.setDescription(seed.description());
        club.setCategory(seed.tags().isEmpty() ? club.getCategory() : seed.tags().get(0));
        club.setCategoryTags(ClubTagCodec.encode(seed.tags(), seed.tags().isEmpty() ? club.getCategory() : seed.tags().get(0)));
        club.setDisplayLocation(seed.baseLocation());
        club.setOpeningStart(seed.openingStart());
        club.setOpeningEnd(seed.openingEnd());
        club.setDisplayCourts(seed.venues().size());
        clubRepository.save(club);

        PlanRefreshCounts planCounts = replaceMembershipPlans(club, seed, ordinal);
        VenueTimeslotRefreshCounts venueCounts = refreshVenuesAndTimeslots(club, seed, ordinal, startDate, endDate);
        CommunityQaReplaceCounts qaCounts = replaceCommunityQa(club, seed, binding.clubAdminUserId(), communityMembers, ordinal);

        return new ClubOperationalRefreshCounts(
                planCounts.deleted(),
                planCounts.created(),
                venueCounts.timeslotsDeleted(),
                venueCounts.timeslotsCreated(),
                qaCounts.questionsDeleted(),
                qaCounts.questionsCreated(),
                qaCounts.answersDeleted(),
                qaCounts.answersCreated()
        );
    }

    private PlanRefreshCounts replaceMembershipPlans(Club club, ClubSeed seed, int ordinal) {
        Integer clubId = club == null ? null : club.getClubId();
        if (clubId == null) {
            return new PlanRefreshCounts(0, 0);
        }

        List<MembershipPlan> existingPlans = membershipPlanRepository.findByClubId(clubId);
        int deleted = existingPlans.size();
        if (!existingPlans.isEmpty()) {
            membershipPlanRepository.deleteAllInBatch(existingPlans);
        }

        List<MembershipPlanSeed> planSeeds = buildMembershipPlanSeeds(seed, ordinal);
        List<MembershipPlan> plans = new ArrayList<>();
        for (MembershipPlanSeed planSeed : planSeeds) {
            MembershipPlan plan = new MembershipPlan();
            plan.setClubId(clubId);
            plan.setPlanCode(planSeed.planCode());
            plan.setBenefitType(planSeed.benefitType());
            plan.setPlanName(planSeed.planName());
            plan.setPrice(planSeed.price());
            plan.setDurationDays(planSeed.durationDays());
            plan.setDiscountPercent(planSeed.discountPercent());
            plan.setIncludedBookings(planSeed.includedBookings());
            plan.setEnabled(planSeed.enabled());
            plan.setDescription(planSeed.description());
            plans.add(plan);
        }
        membershipPlanRepository.saveAll(plans);
        return new PlanRefreshCounts(deleted, plans.size());
    }

    private VenueTimeslotRefreshCounts refreshVenuesAndTimeslots(Club club,
                                                                 ClubSeed seed,
                                                                 int ordinal,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        Integer clubId = club == null ? null : club.getClubId();
        if (clubId == null) {
            return new VenueTimeslotRefreshCounts(0, 0);
        }

        List<Venue> existingVenues = venueRepository.findByClubId(clubId).stream()
                .sorted(Comparator.comparing(Venue::getVenueId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<Integer> existingVenueIds = existingVenues.stream()
                .map(Venue::getVenueId)
                .filter(id -> id != null)
                .toList();

        List<TimeSlot> existingSlots = existingVenueIds.isEmpty()
                ? List.of()
                : timeSlotRepository.findAll().stream()
                .filter(slot -> slot != null && slot.getVenueId() != null && existingVenueIds.contains(slot.getVenueId()))
                .toList();
        int deleted = existingSlots.size();
        if (!existingSlots.isEmpty()) {
            timeSlotRepository.deleteAllInBatch(existingSlots);
        }

        List<Venue> savedVenues = upsertClubVenues(club, seed, existingVenues);
        List<TimeSlot> slots = new ArrayList<>();
        for (int venueIndex = 0; venueIndex < savedVenues.size(); venueIndex++) {
            Venue venue = savedVenues.get(venueIndex);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (!seed.activeDays().contains(date.getDayOfWeek())) {
                    continue;
                }
                List<GeneratedSlotSeed> dailySlots = buildGeneratedSlotsForDate(seed, date, ordinal);
                for (int slotIndex = 0; slotIndex < dailySlots.size(); slotIndex++) {
                    GeneratedSlotSeed generated = dailySlots.get(slotIndex);
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setVenueId(venue.getVenueId());
                    timeSlot.setStartTime(LocalDateTime.of(date, generated.start()));
                    timeSlot.setEndTime(LocalDateTime.of(date, generated.start().plusMinutes(generated.durationMinutes())));
                    timeSlot.setMaxCapacity(venue.getCapacity());
                    timeSlot.setPrice(computeSlotPrice(seed, generated, date, ordinal, venueIndex, slotIndex));
                    slots.add(timeSlot);
                }
            }
        }
        timeSlotRepository.saveAll(slots);
        return new VenueTimeslotRefreshCounts(deleted, slots.size());
    }

    private List<Venue> upsertClubVenues(Club club, ClubSeed seed, List<Venue> existingVenues) {
        List<Venue> saved = new ArrayList<>();
        Integer clubId = club == null ? null : club.getClubId();
        if (clubId == null) {
            return saved;
        }

        for (int index = 0; index < seed.venues().size(); index++) {
            VenueSeed venueSeed = seed.venues().get(index);
            Venue venue = index < existingVenues.size() ? existingVenues.get(index) : new Venue();
            venue.setClubId(clubId);
            venue.setVenueName(venueSeed.name());
            venue.setLocation(seed.baseLocation() + " - " + venueSeed.name());
            venue.setCapacity(venueSeed.capacity());
            saved.add(venueRepository.save(venue));
        }

        if (existingVenues.size() > seed.venues().size()) {
            List<Venue> extras = existingVenues.subList(seed.venues().size(), existingVenues.size());
            if (!extras.isEmpty()) {
                venueRepository.deleteAllInBatch(extras);
            }
        }

        club.setDisplayCourts(saved.size());
        clubRepository.save(club);
        return saved;
    }

    private CommunityQaReplaceCounts replaceCommunityQa(Club club,
                                                        ClubSeed seed,
                                                        Integer clubAdminUserId,
                                                        List<User> communityMembers,
                                                        int ordinal) {
        Integer clubId = club == null ? null : club.getClubId();
        if (clubId == null) {
            return new CommunityQaReplaceCounts(0, 0, 0, 0);
        }

        List<ClubCommunityQuestion> existingQuestions = clubCommunityQuestionRepository
                .findByClubIdOrderByUpdatedAtDescQuestionIdDesc(clubId);
        List<Integer> questionIds = existingQuestions.stream()
                .map(ClubCommunityQuestion::getQuestionId)
                .filter(id -> id != null)
                .toList();
        List<ClubCommunityAnswer> existingAnswers = questionIds.isEmpty()
                ? List.of()
                : clubCommunityAnswerRepository.findByQuestionIdInOrderByCreatedAtAscAnswerIdAsc(questionIds);

        int deletedQuestions = existingQuestions.size();
        int deletedAnswers = existingAnswers.size();
        if (!existingAnswers.isEmpty()) {
            clubCommunityAnswerRepository.deleteAllInBatch(existingAnswers);
        }
        if (!existingQuestions.isEmpty()) {
            clubCommunityQuestionRepository.deleteAllInBatch(existingQuestions);
        }

        List<CommunityThreadSeed> threads = buildCommunityThreads(club, seed, ordinal);
        int createdQuestions = 0;
        int createdAnswers = 0;
        for (int index = 0; index < threads.size(); index++) {
            User member = communityMembers.get(Math.floorMod(ordinal - 1 + index, communityMembers.size()));
            CommunityThreadSeed thread = threads.get(index);
            createCommunityThread(
                    club,
                    clubAdminUserId,
                    member,
                    thread.questionText(),
                    thread.answerText(),
                    thread.questionTime(),
                    thread.answerTime()
            );
            createdQuestions++;
            createdAnswers++;
        }

        return new CommunityQaReplaceCounts(deletedQuestions, createdQuestions, deletedAnswers, createdAnswers);
    }

    private void resetClubDataset() {
        chatMessageRepository.deleteAllInBatch();
        chatSessionRepository.deleteAllInBatch();
        bookingHoldRepository.deleteAllInBatch();
        bookingRecordRepository.deleteAllInBatch();
        checkoutSessionRepository.deleteAllInBatch();
        transactionRecordRepository.deleteAllInBatch();
        clubCommunityAnswerRepository.deleteAllInBatch();
        clubCommunityQuestionRepository.deleteAllInBatch();
        userMembershipRepository.deleteAllInBatch();
        membershipPlanRepository.deleteAllInBatch();
        clubChatKbEntryRepository.deleteAllInBatch();
        clubImageRepository.deleteAllInBatch();
        clubAdminRepository.deleteAllInBatch();
        timeSlotRepository.deleteAllInBatch();
        venueRepository.deleteAllInBatch();
        clubRepository.deleteAllInBatch();
        registrationEmailVerificationRepository.deleteAllInBatch();
        profileEmailChangeVerificationRepository.deleteAllInBatch();
        passwordResetTokenRepository.deleteAllInBatch();

        List<User> clubUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.CLUB)
                .toList();
        if (!clubUsers.isEmpty()) {
            userRepository.deleteAllInBatch(clubUsers);
        }
    }

    private void seedClub(int ordinal,
                          ClubSeed seed,
                          LocalDate startDate,
                          LocalDate endDate,
                          List<User> communityMembers) {
        String accountEmail = ordinal + "@" + ordinal + ".com";

        User user = new User();
        user.setUsername(seed.name());
        user.setEmail(accountEmail);
        user.setPasswordHash(passwordEncryptionUtil.encodePassword(passwordFor(ordinal)));
        user.setRole(User.Role.CLUB);
        user.setSessionVersion(1);
        User savedUser = userRepository.save(user);

        Club club = new Club();
        club.setClubName(seed.name());
        club.setDescription(seed.description());
        club.setCategory(seed.tags().get(0));
        club.setCategoryTags(ClubTagCodec.encode(seed.tags(), seed.tags().get(0)));
        club.setEmail(accountEmail);
        club.setPhone(phoneFor(ordinal));
        club.setDisplayLocation(seed.baseLocation());
        club.setOpeningStart(seed.openingStart());
        club.setOpeningEnd(seed.openingEnd());
        club.setDisplayCourts(seed.venues().size());
        Club savedClub = clubRepository.save(club);

        ClubAdmin clubAdmin = new ClubAdmin();
        clubAdmin.setUserId(savedUser.getUserId());
        clubAdmin.setClubId(savedClub.getClubId());
        clubAdminRepository.save(clubAdmin);

        MembershipPlan monthlyFlex = new MembershipPlan();
        monthlyFlex.setClubId(savedClub.getClubId());
        monthlyFlex.setPlanCode("MONTHLY");
        monthlyFlex.setBenefitType("DISCOUNT");
        monthlyFlex.setPlanName("Monthly Flex");
        monthlyFlex.setPrice(seed.membershipPrice());
        monthlyFlex.setDurationDays(30);
        monthlyFlex.setDiscountPercent(BigDecimal.valueOf(10));
        monthlyFlex.setIncludedBookings(0);
        monthlyFlex.setEnabled(true);
        monthlyFlex.setDescription("Monthly member pricing with a standing 10% discount on bookings.");
        membershipPlanRepository.save(monthlyFlex);

        List<Venue> savedVenues = new ArrayList<>();
        for (VenueSeed venueSeed : seed.venues()) {
            Venue venue = new Venue();
            venue.setClubId(savedClub.getClubId());
            venue.setVenueName(venueSeed.name());
            venue.setLocation(seed.baseLocation() + " - " + venueSeed.name());
            venue.setCapacity(venueSeed.capacity());
            savedVenues.add(venueRepository.save(venue));
        }

        List<TimeSlot> slots = new ArrayList<>();
        for (Venue venue : savedVenues) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (!seed.activeDays().contains(date.getDayOfWeek())) {
                    continue;
                }
                for (SlotSeed slotSeed : seed.slots()) {
                    TimeSlot timeSlot = new TimeSlot();
                    timeSlot.setVenueId(venue.getVenueId());
                    timeSlot.setStartTime(LocalDateTime.of(date, slotSeed.start()));
                    timeSlot.setEndTime(LocalDateTime.of(date, slotSeed.start().plusMinutes(slotSeed.durationMinutes())));
                    timeSlot.setMaxCapacity(venue.getCapacity());
                    timeSlot.setPrice(seed.bookingPrice());
                    slots.add(timeSlot);
                }
            }
        }
        timeSlotRepository.saveAll(slots);
        seedCommunityQa(savedClub, savedUser.getUserId(), communityMembers, ordinal);
    }

    private List<User> ensureCommunityMembers() {
        List<User> members = new ArrayList<>();
        for (CommunityMemberSeed seed : COMMUNITY_MEMBER_SEEDS) {
            User member = userRepository.findByEmailIgnoreCase(seed.email())
                    .orElseGet(() -> {
                        User created = new User();
                        created.setUsername(seed.name());
                        created.setEmail(seed.email());
                        created.setPasswordHash(passwordEncryptionUtil.encodePassword(COMMUNITY_MEMBER_PASSWORD));
                        created.setRole(User.Role.USER);
                        created.setSessionVersion(1);
                        return userRepository.save(created);
                    });
            members.add(member);
        }
        return members;
    }

    private CommunityQaSeedCounts seedCommunityQa(Club club,
                                                  Integer clubAdminUserId,
                                                  List<User> communityMembers,
                                                  int ordinal) {
        if (club == null || club.getClubId() == null || communityMembers == null || communityMembers.isEmpty()) {
            return new CommunityQaSeedCounts(0, 0);
        }

        ClubSeed seed = CLUB_SEEDS.get(Math.max(0, Math.min(CLUB_SEEDS.size() - 1, ordinal - 1)));
        List<CommunityThreadSeed> threads = buildCommunityThreads(club, seed, ordinal);
        int created = 0;
        for (int index = 0; index < threads.size(); index++) {
            User member = communityMembers.get(Math.floorMod(ordinal - 1 + index, communityMembers.size()));
            CommunityThreadSeed thread = threads.get(index);
            createCommunityThread(
                    club,
                    clubAdminUserId,
                    member,
                    thread.questionText(),
                    thread.answerText(),
                    thread.questionTime(),
                    thread.answerTime()
            );
            created++;
        }

        return new CommunityQaSeedCounts(created, created);
    }

    private void createCommunityThread(Club club,
                                       Integer clubAdminUserId,
                                       User member,
                                       String questionText,
                                       String answerText,
                                       LocalDateTime questionTime,
                                       LocalDateTime answerTime) {
        if (club == null || club.getClubId() == null || member == null || member.getUserId() == null) {
            return;
        }

        ClubCommunityQuestion question = new ClubCommunityQuestion();
        question.setClubId(club.getClubId());
        question.setUserId(member.getUserId());
        question.setQuestionText(questionText);
        question.setCreatedAt(questionTime);
        question.setUpdatedAt(answerTime);
        ClubCommunityQuestion savedQuestion = clubCommunityQuestionRepository.save(question);

        ClubCommunityAnswer answer = new ClubCommunityAnswer();
        answer.setQuestionId(savedQuestion.getQuestionId());
        answer.setClubId(club.getClubId());
        answer.setUserId(clubAdminUserId);
        answer.setResponderType(ClubCommunityAnswer.ResponderType.CLUB);
        answer.setAnswerText(answerText);
        answer.setCreatedAt(answerTime);
        clubCommunityAnswerRepository.save(answer);
    }

    private List<MembershipPlanSeed> buildMembershipPlanSeeds(ClubSeed seed, int ordinal) {
        String primaryTag = normalizeTag(seed.tags().isEmpty() ? "" : seed.tags().get(0));
        String sportTheme = membershipTheme(primaryTag, ordinal);
        BigDecimal monthlyPrice = normalizeMoney(seed.membershipPrice());
        BigDecimal monthlyDiscount = normalizeMoney(BigDecimal.valueOf(8 + (Math.floorMod(ordinal, 5) * 2)));
        BigDecimal quarterlyPrice = normalizeMoney(
                monthlyPrice.multiply(BigDecimal.valueOf(2.55 + (Math.floorMod(ordinal, 3) * 0.12)))
        );
        BigDecimal quarterlyDiscount = normalizeMoney(monthlyDiscount.add(BigDecimal.valueOf(5 + Math.floorMod(ordinal, 2))));
        int packBookings = 4 + (Math.floorMod(ordinal, 3) * 2);
        BigDecimal bookingPackPrice = normalizeMoney(
                seed.bookingPrice()
                        .multiply(BigDecimal.valueOf(packBookings))
                        .multiply(BigDecimal.valueOf(0.86 + (Math.floorMod(ordinal, 2) * 0.04)))
        );

        String activeDays = describeActiveDays(seed.activeDays());
        String venueName = firstNonBlank(seed.venues().isEmpty() ? null : seed.venues().get(0).name(), seed.baseLocation());

        return List.of(
                new MembershipPlanSeed(
                        "MONTHLY",
                        "DISCOUNT",
                        sportTheme + " Monthly",
                        monthlyPrice,
                        30,
                        monthlyDiscount,
                        0,
                        true,
                        "Best for regular bookings on " + activeDays + ", with member pricing across the main weekly blocks."
                ),
                new MembershipPlanSeed(
                        "QUARTERLY",
                        "DISCOUNT",
                        sportTheme + " Quarter",
                        quarterlyPrice,
                        90,
                        quarterlyDiscount,
                        0,
                        true,
                        "Lower per-session cost for members booking consistently at " + venueName + " over a full training term."
                ),
                new MembershipPlanSeed(
                        "PACK_" + String.format(Locale.ROOT, "%02d", ordinal),
                        "BOOKING_PACK",
                        packBookings + "-Session " + sportTheme + " Pack",
                        bookingPackPrice,
                        60,
                        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                        packBookings,
                        true,
                        "Good for lighter attendance: includes " + packBookings + " prepaid bookings that can be used flexibly within 60 days."
                )
        );
    }

    private List<GeneratedSlotSeed> buildGeneratedSlotsForDate(ClubSeed seed, LocalDate date, int ordinal) {
        List<GeneratedSlotSeed> generated = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < seed.slots().size(); slotIndex++) {
            SlotSeed slotSeed = seed.slots().get(slotIndex);
            generated.add(new GeneratedSlotSeed(
                    adjustSlotStart(slotSeed, date, ordinal, slotIndex),
                    adjustSlotDuration(slotSeed, date, ordinal, slotIndex),
                    false
            ));
        }

        GeneratedSlotSeed showcase = buildShowcaseSlot(seed, date, ordinal);
        if (showcase != null) {
            generated.add(showcase);
        }
        generated.sort(Comparator.comparing(GeneratedSlotSeed::start));
        return generated;
    }

    private LocalTime adjustSlotStart(SlotSeed slotSeed, LocalDate date, int ordinal, int slotIndex) {
        int shiftMinutes = switch (date.getDayOfWeek()) {
            case SATURDAY -> 15;
            case SUNDAY -> 25;
            case FRIDAY -> slotIndex == 0 ? 0 : 15;
            default -> Math.floorMod(ordinal + slotIndex, 2) * 5;
        };
        LocalTime shifted = slotSeed.start().plusMinutes(shiftMinutes);
        LocalTime floor = LocalTime.of(6, 0);
        return shifted.isBefore(floor) ? floor : shifted;
    }

    private int adjustSlotDuration(SlotSeed slotSeed, LocalDate date, int ordinal, int slotIndex) {
        int duration = slotSeed.durationMinutes();
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY && slotIndex == seedLastSlotIndex(ordinal)) {
            return duration + 15;
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY && slotIndex == 0) {
            return duration + 15;
        }
        if (Math.floorMod(ordinal + slotIndex, 5) == 0) {
            return duration + 10;
        }
        return duration;
    }

    private int seedLastSlotIndex(int ordinal) {
        return Math.max(0, Math.floorMod(ordinal, 2));
    }

    private GeneratedSlotSeed buildShowcaseSlot(ClubSeed seed, LocalDate date, int ordinal) {
        if (!isShowcaseDay(seed, date, ordinal) || seed.slots().isEmpty()) {
            return null;
        }
        SlotSeed last = seed.slots().get(seed.slots().size() - 1);
        LocalTime close = parseSeedTime(seed.openingEnd(), LocalTime.of(22, 0));
        int duration = 60 + (Math.floorMod(ordinal, 2) * 15);
        LocalTime start = adjustSlotStart(last, date, ordinal, seed.slots().size() - 1)
                .plusMinutes(last.durationMinutes() + 25L);
        LocalTime latestStart = close.minusMinutes(duration + 10L);
        if (latestStart.isBefore(last.start())) {
            return null;
        }
        if (start.isAfter(latestStart)) {
            start = latestStart;
        }
        if (start.plusMinutes(duration).isAfter(close)) {
            return null;
        }
        return new GeneratedSlotSeed(start, duration, true);
    }

    private boolean isShowcaseDay(ClubSeed seed, LocalDate date, int ordinal) {
        List<DayOfWeek> activeDays = seed.activeDays().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList();
        if (activeDays.isEmpty()) {
            return false;
        }
        DayOfWeek showcaseDay = activeDays.get(Math.floorMod(ordinal, activeDays.size()));
        return date.getDayOfWeek() == showcaseDay;
    }

    private LocalTime parseSeedTime(String raw, LocalTime fallback) {
        String text = safe(raw);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return LocalTime.parse(text);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private BigDecimal computeSlotPrice(ClubSeed seed,
                                        GeneratedSlotSeed generated,
                                        LocalDate date,
                                        int ordinal,
                                        int venueIndex,
                                        int slotIndex) {
        BigDecimal price = seed.bookingPrice();

        if (generated.start().isBefore(LocalTime.of(8, 0))) {
            price = price.subtract(BigDecimal.valueOf(1.50));
        } else if (!generated.start().isBefore(LocalTime.of(19, 0))) {
            price = price.add(BigDecimal.valueOf(2.50));
        } else if (!generated.start().isBefore(LocalTime.of(17, 0))) {
            price = price.add(BigDecimal.valueOf(1.25));
        } else {
            price = price.subtract(BigDecimal.valueOf(0.75));
        }

        if (generated.durationMinutes() >= 90) {
            price = price.add(BigDecimal.valueOf(1.00));
        }
        if (generated.durationMinutes() >= 105) {
            price = price.add(BigDecimal.valueOf(1.25));
        }

        switch (date.getDayOfWeek()) {
            case FRIDAY -> price = price.add(BigDecimal.valueOf(0.75));
            case SATURDAY -> price = price.add(BigDecimal.valueOf(1.75));
            case SUNDAY -> price = price.add(BigDecimal.valueOf(2.25));
            default -> {
            }
        }

        if (venueIndex > 0) {
            price = price.add(BigDecimal.valueOf(0.75 + (Math.floorMod(ordinal, 3) * 0.50)));
        }
        if (slotIndex > 0) {
            price = price.add(BigDecimal.valueOf(0.50 * slotIndex));
        }
        if (generated.showcase()) {
            price = price.add(BigDecimal.valueOf(2.25));
        }

        return normalizeMoney(price);
    }

    private List<CommunityThreadSeed> buildCommunityThreads(Club club, ClubSeed seed, int ordinal) {
        List<String> tags = ClubTagCodec.decode(club.getCategoryTags(), club.getCategory());
        String primaryTag = firstNonBlank(tags.isEmpty() ? null : tags.get(0), firstNonBlank(club.getCategory(), "training"));
        String secondaryTag = tags.size() > 1 ? safe(tags.get(1)) : "";
        String sportLabel = secondaryTag.isBlank()
                ? primaryTag.toLowerCase(Locale.ROOT)
                : primaryTag.toLowerCase(Locale.ROOT) + " and " + secondaryTag.toLowerCase(Locale.ROOT);
        String clubName = firstNonBlank(club.getClubName(), "the club");
        String location = firstNonBlank(club.getDisplayLocation(), seed.baseLocation());
        String firstVenue = firstNonBlank(seed.venues().isEmpty() ? null : seed.venues().get(0).name(), location);
        String openingWindow = buildOpeningWindow(club);
        String activeDays = describeActiveDays(seed.activeDays());
        String equipment = equipmentHint(primaryTag, secondaryTag);
        String grouping = groupingHint(primaryTag);
        String sessionType = sessionTypeHint(primaryTag);
        String packAdvice = bookingPackAdvice(primaryTag);

        LocalDate baseDate = LocalDate.now(LONDON);
        return List.of(
                new CommunityThreadSeed(
                        switch (Math.floorMod(ordinal, 3)) {
                            case 0 -> "Which listed " + sessionType + " at " + clubName + " is the easiest first booking for someone new to " + sportLabel + "?";
                            case 1 -> "I only know the basics of " + sportLabel + ". Would you point a beginner toward the earlier session or the later one at " + clubName + "?";
                            default -> "For a first visit to " + clubName + ", which " + sportLabel + " block is usually the most beginner-friendly?";
                        },
                        switch (Math.floorMod(ordinal, 3)) {
                            case 0 -> "Start with the steadier introductory block rather than the busiest peak-time session. Arrive 10 minutes early and the team can explain the format before you join in.";
                            case 1 -> "The earlier block is usually the softer landing for new members, especially if you want a short walkthrough before the session starts.";
                            default -> "Pick one of the lighter rotation sessions first. We normally keep the first booking low-pressure and can direct you to the most suitable group on arrival.";
                        },
                        baseDate.minusDays(10L + Math.floorMod(ordinal, 4)).atTime(18, 15),
                        baseDate.minusDays(10L + Math.floorMod(ordinal, 4)).atTime(22, 5)
                ),
                new CommunityThreadSeed(
                        switch (clubArchetype(primaryTag)) {
                            case "outdoor" -> "If the weather turns at " + location + ", does " + clubName + " usually cancel early enough for members to rebook another session?";
                            case "team" -> "If I come alone to " + firstVenue + ", do you help place solo members into a balanced group or should I arrive with teammates?";
                            case "combat" -> "Do I need to bring my own " + equipment + " on the first visit, or does " + clubName + " have loan kit for trial sessions?";
                            case "studio" -> "Is there anything specific I should bring for the first class at " + firstVenue + ", or is standard kit enough?";
                            default -> "If I book on my own at " + firstVenue + ", can the club help match me by level, and should I arrive early " + openingWindow + "?";
                        },
                        switch (clubArchetype(primaryTag)) {
                            case "outdoor" -> "Yes. We normally post updates as early as possible and move people into the next suitable block when weather affects the planned session.";
                            case "team" -> "Yes. Solo members are regularly slotted into balanced groups, so arriving a little early helps staff place you in the right run of play.";
                            case "combat" -> "For a first visit we can usually help with basic loan kit, but bringing your own well-fitted gear becomes the better option once you book regularly.";
                            case "studio" -> "Standard training kit is usually enough on day one. If there is any class-specific requirement, staff will flag it when you check in.";
                            default -> "Yes. We can usually help with " + grouping + ", and arriving around 10 minutes early gives the team time to point you to the correct court, lane, or studio area.";
                        },
                        baseDate.minusDays(6L + Math.floorMod(ordinal, 3)).atTime(9, 20),
                        baseDate.minusDays(6L + Math.floorMod(ordinal, 3)).atTime(12, 10)
                ),
                new CommunityThreadSeed(
                        switch (clubArchetype(primaryTag)) {
                            case "studio" -> "If I can only make one or two sessions a week on " + activeDays + ", is the booking pack better value than the monthly membership at " + clubName + "?";
                            case "outdoor" -> "For " + clubName + ", is the monthly membership mainly worth it for peak sessions, or is the booking pack usually enough for casual bookings?";
                            default -> "I am comparing the membership options at " + clubName + ". Which plan makes most sense if I expect to book about twice a week?";
                        },
                        switch (clubArchetype(primaryTag)) {
                            case "studio" -> "If your pattern is light and flexible, the booking pack is normally the cleaner option. The monthly membership starts to win once you are booking most weeks.";
                            case "outdoor" -> "The monthly plan usually helps most if you want the more in-demand evening slots regularly. " + packAdvice;
                            default -> "For steady weekly attendance, the monthly or quarterly plan usually gives the strongest value. " + packAdvice;
                        },
                        baseDate.minusDays(3L + Math.floorMod(ordinal, 2)).atTime(19, 5),
                        baseDate.minusDays(3L + Math.floorMod(ordinal, 2)).atTime(21, 45)
                )
        );
    }

    private String membershipTheme(String primaryTag, int ordinal) {
        return switch (normalizeTag(primaryTag)) {
            case "badminton" -> "Rally Flex";
            case "basketball" -> "Hoops Pass";
            case "football" -> "Matchday Pass";
            case "tennis" -> "Baseline Club";
            case "volleyball" -> "Spikers Pass";
            case "swimming" -> "Lane Access";
            case "running" -> "Pace Crew";
            case "cycling" -> "Ride Lab";
            case "yoga" -> "Stillpoint Flow";
            case "table tennis" -> "Spin Session";
            case "boxing" -> "Fight Camp";
            case "cricket" -> "Net Session";
            case "rugby" -> "Contact Block";
            case "climbing" -> "Summit Access";
            case "fencing" -> "Foil Room";
            case "rowing" -> "Harbour Stroke";
            case "golf" -> "Swing Studio";
            case "pilates" -> "Coreline Pass";
            case "dance" -> "Performance Pass";
            case "judo" -> "Dojo Pass";
            default -> (Math.floorMod(ordinal, 2) == 0 ? "Peak Access" : "Club Flex");
        };
    }

    private String equipmentHint(String primaryTag, String secondaryTag) {
        return switch (normalizeTag(primaryTag)) {
            case "badminton" -> secondaryTag.isBlank() ? "a racket and indoor shoes" : "a racket, paddle, and indoor shoes";
            case "basketball" -> "indoor shoes and a ball if you want your own warm-up";
            case "football" -> "boots or turf shoes and shin protection";
            case "tennis" -> secondaryTag.isBlank() ? "a racket and court shoes" : "a racket, padel bat, and court shoes";
            case "volleyball" -> "court shoes and kneepads if you use them";
            case "swimming" -> "goggles, cap, and your usual swim kit";
            case "running" -> "running shoes suited to your pace and distance";
            case "cycling" -> "cycling shoes if you use clip-ins, otherwise standard trainers work";
            case "yoga", "pilates" -> "comfortable movement kit and a mat if you prefer your own";
            case "table tennis" -> "a bat if you have one, although spare paddles are often available";
            case "boxing" -> "gloves and wraps";
            case "cricket" -> "batting gloves or personal kit if you prefer not to use club spares";
            case "rugby" -> "boots plus any usual contact protection";
            case "climbing" -> "climbing shoes and chalk";
            case "fencing" -> "basic fencing kit and glove";
            case "rowing" -> "close-fitting training kit and stable shoes";
            case "golf" -> "glove and clubs if you want your own setup";
            case "dance" -> "training clothes and the footwear used for your class";
            case "judo" -> "a judogi if you already have one";
            default -> "standard training kit";
        };
    }

    private String groupingHint(String primaryTag) {
        return switch (clubArchetype(primaryTag)) {
            case "team" -> "balanced groups by experience and intensity";
            case "endurance" -> "pace groups so people are not left on their own";
            case "studio" -> "the most suitable class pace";
            default -> "matching by level";
        };
    }

    private String sessionTypeHint(String primaryTag) {
        return switch (normalizeTag(primaryTag)) {
            case "basketball" -> "scrimmage block";
            case "football" -> "small-sided block";
            case "swimming" -> "lane session";
            case "running" -> "pace-group session";
            case "cycling" -> "interval ride";
            case "yoga", "pilates" -> "class block";
            case "boxing", "judo", "fencing" -> "technical session";
            case "dance" -> "training block";
            default -> "session";
        };
    }

    private String bookingPackAdvice(String primaryTag) {
        return switch (clubArchetype(primaryTag)) {
            case "team" -> "If you only join the occasional run of games, the pack is usually enough.";
            case "endurance" -> "If your attendance changes week to week, the pack usually keeps more flexibility.";
            case "studio" -> "If you are dipping in around work, the pack keeps your costs tighter.";
            default -> "If you are only booking occasionally, the pack usually covers lighter attendance more cleanly.";
        };
    }

    private String clubArchetype(String primaryTag) {
        return switch (normalizeTag(primaryTag)) {
            case "basketball", "football", "volleyball", "rugby" -> "team";
            case "running", "cycling", "rowing", "swimming" -> "endurance";
            case "yoga", "pilates", "dance" -> "studio";
            case "boxing", "judo", "fencing" -> "combat";
            case "golf", "cricket" -> "outdoor";
            default -> "general";
        };
    }

    private String describeActiveDays(Set<DayOfWeek> activeDays) {
        if (activeDays == null || activeDays.isEmpty()) {
            return "selected days";
        }
        List<String> labels = activeDays.stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .map(day -> day.getDisplayName(TextStyle.SHORT, Locale.UK))
                .toList();
        if (labels.size() == 1) {
            return labels.get(0);
        }
        if (labels.size() == 2) {
            return labels.get(0) + " and " + labels.get(1);
        }
        return String.join(", ", labels.subList(0, labels.size() - 1)) + ", and " + labels.get(labels.size() - 1);
    }

    private BigDecimal normalizeMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeTag(String value) {
        return safe(value).toLowerCase(Locale.ROOT);
    }

    private static String buildOpeningWindow(Club club) {
        String openingStart = safe(club == null ? null : club.getOpeningStart());
        String openingEnd = safe(club == null ? null : club.getOpeningEnd());
        if (openingStart.isBlank() || openingEnd.isBlank()) {
            return "before the session starts";
        }
        return "between " + openingStart + " and " + openingEnd;
    }

    private static String firstNonBlank(String value, String fallback) {
        String text = safe(value);
        return text.isBlank() ? fallback : text;
    }

    private static String passwordFor(int ordinal) {
        String token = String.valueOf(ordinal);
        StringBuilder builder = new StringBuilder(6);
        while (builder.length() < 6) {
            builder.append(token);
        }
        return builder.substring(0, 6);
    }

    private static String phoneFor(int ordinal) {
        return String.format("0203%07d", ordinal);
    }

    private static ClubSeed club(String name,
                                 String description,
                                 String baseLocation,
                                 List<String> tags,
                                 String openingStart,
                                 String openingEnd,
                                 Set<DayOfWeek> activeDays,
                                 List<VenueSeed> venues,
                                 List<SlotSeed> slots,
                                 double bookingPrice,
                                 double membershipPrice) {
        return new ClubSeed(
                name,
                description,
                baseLocation,
                tags,
                openingStart,
                openingEnd,
                activeDays,
                venues,
                slots,
                BigDecimal.valueOf(bookingPrice),
                BigDecimal.valueOf(membershipPrice)
        );
    }

    private static VenueSeed venue(String name, int capacity) {
        return new VenueSeed(name, capacity);
    }

    private static SlotSeed slot(String start, int durationMinutes) {
        return new SlotSeed(LocalTime.parse(start), durationMinutes);
    }

    private static CommunityMemberSeed member(String name, String email) {
        return new CommunityMemberSeed(name, email);
    }

    public record CurrentClubSnapshot(
            List<String> clubNames,
            List<String> clubEmails
    ) {
        public int clubCount() {
            return clubNames.size();
        }

        public int clubUserCount() {
            return clubEmails.size();
        }
    }

    public record SeedExecutionResult(
            List<String> previousClubNames,
            List<String> previousClubEmails,
            LocalDate startDate,
            LocalDate endDate,
            int clubCount
    ) {
    }

    public record CommunityQaSeedResult(
            int clubsUpdated,
            int questionsCreated,
            int answersCreated
    ) {
    }

    public record CanonicalContentRefreshResult(
            int clubsUpdated,
            int plansDeleted,
            int plansCreated,
            int timeslotsDeleted,
            int timeslotsCreated,
            int questionsDeleted,
            int questionsCreated,
            int answersDeleted,
            int answersCreated,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    private record ClubSeed(
            String name,
            String description,
            String baseLocation,
            List<String> tags,
            String openingStart,
            String openingEnd,
            Set<DayOfWeek> activeDays,
            List<VenueSeed> venues,
            List<SlotSeed> slots,
            BigDecimal bookingPrice,
            BigDecimal membershipPrice
    ) {
    }

    private record VenueSeed(String name, int capacity) {
    }

    private record SlotSeed(LocalTime start, int durationMinutes) {
    }

    private record CommunityMemberSeed(String name, String email) {
    }

    private record CommunityQaSeedCounts(int questionsCreated, int answersCreated) {
    }

    private record GeneratedSlotSeed(LocalTime start, int durationMinutes, boolean showcase) {
    }

    private record MembershipPlanSeed(
            String planCode,
            String benefitType,
            String planName,
            BigDecimal price,
            int durationDays,
            BigDecimal discountPercent,
            int includedBookings,
            boolean enabled,
            String description
    ) {
    }

    private record CommunityThreadSeed(
            String questionText,
            String answerText,
            LocalDateTime questionTime,
            LocalDateTime answerTime
    ) {
    }

    private record CanonicalClubBinding(int ordinal, Club club, ClubSeed seed, Integer clubAdminUserId) {
    }

    private record CanonicalRefreshGuard(long membershipsInUse, long bookingRecords, long bookingHolds) {
        boolean hasBlockingData() {
            return membershipsInUse > 0 || bookingRecords > 0 || bookingHolds > 0;
        }
    }

    private record ClubOperationalRefreshCounts(
            int plansDeleted,
            int plansCreated,
            int timeslotsDeleted,
            int timeslotsCreated,
            int questionsDeleted,
            int questionsCreated,
            int answersDeleted,
            int answersCreated
    ) {
    }

    private record PlanRefreshCounts(int deleted, int created) {
    }

    private record VenueTimeslotRefreshCounts(int timeslotsDeleted, int timeslotsCreated) {
    }

    private record CommunityQaReplaceCounts(
            int questionsDeleted,
            int questionsCreated,
            int answersDeleted,
            int answersCreated
    ) {
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
