import AVFoundation
import SwiftUI
import UniformTypeIdentifiers

private struct AudioEntry: Identifiable, Equatable {
    let id: UUID
    let title: String
    let url: URL
}

@MainActor
private final class AudioLibrary: NSObject, ObservableObject, AVAudioPlayerDelegate {
    @Published private(set) var entries: [AudioEntry] = []
    @Published private(set) var currentID: UUID?
    @Published private(set) var isPlaying = false
    @Published private(set) var currentTime: TimeInterval = 0
    @Published private(set) var duration: TimeInterval = 0
    @Published var message: String?

    private var player: AVAudioPlayer?
    private var timer: Timer?
    private let folder: URL

    override init() {
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        folder = documents.appendingPathComponent("Music", isDirectory: true)
        super.init()
        try? FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
        loadSavedFiles()
    }

    func importFiles(_ urls: [URL]) {
        var imported = 0
        for source in urls {
            let hasAccess = source.startAccessingSecurityScopedResource()
            defer { if hasAccess { source.stopAccessingSecurityScopedResource() } }
            do {
                let destination = uniqueDestination(for: source.lastPathComponent)
                try FileManager.default.copyItem(at: source, to: destination)
                entries.append(AudioEntry(id: UUID(), title: source.deletingPathExtension().lastPathComponent, url: destination))
                imported += 1
            } catch {
                message = "Não foi possível importar \(source.lastPathComponent): \(error.localizedDescription)"
            }
        }
        if imported > 0 { message = "\(imported) ficheiro(s) adicionado(s) à biblioteca." }
    }

    func play(_ entry: AudioEntry) {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            player?.stop()
            let next = try AVAudioPlayer(contentsOf: entry.url)
            next.delegate = self
            next.prepareToPlay()
            player = next
            currentID = entry.id
            duration = next.duration
            currentTime = 0
            isPlaying = next.play()
            startTimer()
        } catch {
            message = "Não foi possível reproduzir este ficheiro: \(error.localizedDescription)"
        }
    }

    func togglePlayback() {
        guard let player else { return }
        if player.isPlaying { player.pause(); isPlaying = false }
        else { isPlaying = player.play() }
    }

    func skip(by offset: Int) {
        guard !entries.isEmpty else { return }
        let index = entries.firstIndex(where: { $0.id == currentID }) ?? 0
        let nextIndex = (index + offset + entries.count) % entries.count
        play(entries[nextIndex])
    }

    func seek(to value: Double) {
        player?.currentTime = value
        currentTime = value
    }

    var currentTitle: String { entries.first(where: { $0.id == currentID })?.title ?? "Nada a reproduzir" }

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        isPlaying = false
        currentTime = duration
    }

    private func loadSavedFiles() {
        let files = (try? FileManager.default.contentsOfDirectory(at: folder, includingPropertiesForKeys: nil)) ?? []
        entries = files.filter { UTType(filenameExtension: $0.pathExtension)?.conforms(to: .audio) == true }
            .sorted { $0.lastPathComponent.localizedStandardCompare($1.lastPathComponent) == .orderedAscending }
            .map { AudioEntry(id: UUID(), title: $0.deletingPathExtension().lastPathComponent, url: $0) }
    }

    private func uniqueDestination(for name: String) -> URL {
        let candidate = folder.appendingPathComponent(name)
        guard FileManager.default.fileExists(atPath: candidate.path) else { return candidate }
        let ext = candidate.pathExtension
        let stem = candidate.deletingPathExtension().lastPathComponent
        return folder.appendingPathComponent("\(stem)-\(UUID().uuidString.prefix(8)).\(ext)")
    }

    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true) { [weak self] _ in
            Task { @MainActor in
                guard let self, let player = self.player else { return }
                self.currentTime = player.currentTime
                self.isPlaying = player.isPlaying
            }
        }
    }
}

struct LibraryView: View {
    @StateObject private var library = AudioLibrary()
    @State private var showingImporter = false

    var body: some View {
        NavigationStack {
            Group {
                if library.entries.isEmpty {
                    ContentUnavailableView("A biblioteca está vazia", systemImage: "music.note.list", description: Text("Importe ficheiros de áudio para começar a ouvir música no GramPlay."))
                } else {
                    List(library.entries) { entry in
                        Button { library.play(entry) } label: {
                            HStack(spacing: 12) {
                                Image(systemName: entry.id == library.currentID && library.isPlaying ? "waveform" : "music.note")
                                    .foregroundStyle(.green)
                                    .frame(width: 28)
                                Text(entry.title).lineLimit(1).foregroundStyle(.primary)
                                Spacer()
                                if entry.id == library.currentID { Image(systemName: "speaker.wave.2.fill").foregroundStyle(.green) }
                            }
                        }
                        .listRowBackground(Color.white.opacity(0.04))
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("GramPlay")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { showingImporter = true } label: { Label("Importar áudio", systemImage: "plus") }
                }
            }
            .safeAreaInset(edge: .bottom) { playerBar }
            .fileImporter(isPresented: $showingImporter, allowedContentTypes: [.audio], allowsMultipleSelection: true) { result in
                switch result {
                case .success(let urls): library.importFiles(urls)
                case .failure(let error): library.message = "Importação cancelada ou falhada: \(error.localizedDescription)"
                }
            }
            .alert("GramPlay", isPresented: Binding(get: { library.message != nil }, set: { if !$0 { library.message = nil } })) {
                Button("OK", role: .cancel) { library.message = nil }
            } message: { Text(library.message ?? "") }
        }
        .tint(.green)
    }

    private var playerBar: some View {
        VStack(spacing: 8) {
            HStack {
                VStack(alignment: .leading, spacing: 3) {
                    Text(library.currentTitle).font(.subheadline.weight(.semibold)).lineLimit(1)
                    Text("Ficheiros neste dispositivo").font(.caption).foregroundStyle(.secondary)
                }
                Spacer()
                Button { library.skip(by: -1) } label: { Image(systemName: "backward.end.fill") }
                Button { library.togglePlayback() } label: { Image(systemName: library.isPlaying ? "pause.fill" : "play.fill").font(.title2).frame(width: 44) }
                    .disabled(library.currentID == nil)
                Button { library.skip(by: 1) } label: { Image(systemName: "forward.end.fill") }
            }
            Slider(value: Binding(get: { library.currentTime }, set: library.seek), in: 0...max(library.duration, 1))
                .tint(.green)
                .disabled(library.currentID == nil)
        }
        .padding(.horizontal, 18)
        .padding(.top, 12)
        .padding(.bottom, 8)
        .background(.ultraThinMaterial)
    }
}
