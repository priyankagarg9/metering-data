package com.meter.energy.consumption.service.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meter.energy.consumption.controller.dto.MeterReadingDto;
import com.meter.energy.consumption.controller.dto.ProfileFractionDto;
import com.meter.energy.consumption.exception.ApplicationException;
import com.meter.energy.consumption.service.BulkMeteringDataService;
import com.meter.energy.consumption.service.ProcessCsvDataService;

/**
 * Implementation of {@link ProcessCsvDataService}.
 */
@Service
public class ProcessCsvDataServiceImpl implements ProcessCsvDataService {

	@Autowired
	private BulkMeteringDataService bulkMeteringDataService;

	public static final String CSV_EXTENSION = "csv";
	public static final String FRACTION = "fractions";
	public static final String READING = "readings";

	public static final String FILE_PATH = "/Users/priyanka/spring_boot_ws/metering-data/src/main/resources/legacyFileProcessing/";

	@Override
	public void processCsvFiles() {
		try {
			// Creates a instance of WatchService.
			WatchService watcher = FileSystems.getDefault().newWatchService();

			// Registers the directory below with a watch service.
			Path logDir = Paths.get(FILE_PATH);
			logDir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

			// Monitor the directory at listen for change notification.
			while (true) {
				WatchKey key = watcher.take();
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (ENTRY_CREATE.equals(kind) || ENTRY_MODIFY.equals(kind)) {
						// Get the name of created file.
						WatchEvent<Path> ev = cast(event);
						Path file = ev.context();
						try {
							// the file extension should be csv else ignore the file
							if (CSV_EXTENSION.equals(FilenameUtils.getExtension(file.getFileName().toString()))) {
								// call the appropriate endpoint according to file name
								if (file.getFileName().toString().startsWith(FRACTION)) {
									processFractionsFile(file);
								} else if (file.getFileName().toString().startsWith(READING)) {
									processReadingsFile(file);
								}
								// deleting the file if processed successfully
								Files.delete(Paths.get(FILE_PATH + file.getFileName().toString()));
							}
						} catch (ApplicationException ex) {
							// handle exceptions by writing log files
							Files.write(Paths
									.get(FILE_PATH + FilenameUtils.getBaseName(file.getFileName().toString()) + ".log"),
									ex.getMessage().getBytes());

						}
					}
				}
				key.reset();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void processReadingsFile(Path file) {
		Pattern pattern = Pattern.compile(",");
		try (BufferedReader in = new BufferedReader(new FileReader(FILE_PATH + file.getFileName().toString()));) {
			List<MeterReadingDto> meterReadings = in.lines().skip(1).map(line -> {
				String[] x = pattern.split(line);
				return new MeterReadingDto((x[0]), x[1], Month.valueOf(x[2]), Integer.parseInt(x[3]));
			}).collect(Collectors.toList());
			Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
			if (rejectedProfiles != null && !rejectedProfiles.isEmpty()) {
				StringBuilder errorMessage = new StringBuilder("Data for the following profiles was rejected: ");
				for (String key : rejectedProfiles.keySet()) {
					errorMessage.append(key + " ");
				}
				throw new ApplicationException(errorMessage.toString());
			}
		} catch (IOException ex) {
			throw new ApplicationException(ex.getMessage());
		}

	}

	private void processFractionsFile(Path file) {
		Pattern pattern = Pattern.compile(",");
		try (BufferedReader in = new BufferedReader(new FileReader(FILE_PATH + file.getFileName().toString()));) {
			List<ProfileFractionDto> fractions = in.lines().skip(1).map(line -> {
				String[] x = pattern.split(line);
				return new ProfileFractionDto(Month.valueOf(x[0]), x[1], Double.parseDouble(x[2]));
			}).collect(Collectors.toList());
			bulkMeteringDataService.processProfileFractions(fractions);
		} catch (IOException ex) {
			throw new ApplicationException(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}
}
